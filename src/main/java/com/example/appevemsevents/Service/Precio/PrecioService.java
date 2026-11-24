package com.example.appevemsevents.Service.Precio;

import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Service.Exception.ResourceNotFoundException;
import com.example.appevecommon.Service.UpdatableService;
import com.example.appevecommon.Service.Utilities.Responses.Ok.PageResult;
import com.example.appevemsevents.Config.RedisCacheConfig;
import com.example.appevemsevents.Repository.IPrecioRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PrecioService extends UpdatableService<SectorByBatch, SectorByBatchDto, UpdateSectorByBatchDto, PrecioMapper, PrecioFilter> {

    private final CacheManager cacheManager;
    private final IPrecioRepository precioRepository;

    protected PrecioService(IPrecioRepository repository, PrecioMapper mapper, CacheManager cacheManager){
        super(repository,mapper);
        this.precioRepository = repository;
        this.cacheManager = cacheManager;
    }

    @Override
    public Specification<SectorByBatch> toSpecification(PrecioFilter filter) {
        return (root, query, criteriaBuilder) -> {
            if(filter == null) return criteriaBuilder.conjunction();
            List<Predicate> predicateList = new ArrayList<>();
            if(filter.getPrice() != null) {
                predicateList.add(
                        criteriaBuilder.equal(root.get("price"), filter.getPrice())
                );
            }
            if (filter.getEventId() != null) {
                predicateList.add(criteriaBuilder.equal(
                        root.get("sector").get("event").get("id"), filter.getEventId()));
                predicateList.add(criteriaBuilder.equal(
                        root.get("batch").get("event").get("id"), filter.getEventId()));
            }

            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')")
    public List<SectorByBatchDto> getByIdsForEvent(Long eventId, List<Long> ids) {
        if (eventId == null || eventId <= 0) {
            throw new InvalidPriceLookupException("eventId debe ser un entero positivo.");
        }
        if (ids == null || ids.isEmpty()) {
            throw new InvalidPriceLookupException("Debe enviarse al menos un ID de precio.");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new InvalidPriceLookupException(
                        "Cada ID de precio debe ser un entero positivo.");
            }
            uniqueIds.add(id);
        }

        List<SectorByBatch> records = repository.findAllById(uniqueIds);
        Map<Long, SectorByBatch> recordsById = new LinkedHashMap<>();
        for (SectorByBatch record : records) {
            recordsById.put(record.getId(), record);
        }

        for (Long requestedId : uniqueIds) {
            SectorByBatch record = recordsById.get(requestedId);
            if (record == null) {
                // BaseEntity's SQL restriction excludes archived records here.
                throw new ResourceNotFoundException(requestedId);
            }

            if (record.getPrice() < 1) {
                throw new InvalidPriceLookupException(
                        "El precio asociado al ID " + requestedId + " no es válido.");
            }
            if (record.getSector() == null
                    || record.getSector().getEvent() == null
                    || record.getBatch() == null
                    || record.getBatch().getEvent() == null) {
                throw new InvalidPriceLookupException(
                        "El ID " + requestedId + " no tiene sector, tanda y evento válidos.");
            }

            Long sectorEventId = record.getSector().getEvent().getId();
            Long batchEventId = record.getBatch().getEvent().getId();
            if (!eventId.equals(sectorEventId) || !eventId.equals(batchEventId)) {
                throw new InvalidPriceLookupException(
                        "El ID " + requestedId + " no pertenece al evento " + eventId + ".");
            }
        }

        return uniqueIds.stream()
                .map(recordsById::get)
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('Cliente')")
    public List<SectorByBatchDto> consumeStockForEvent(Long eventId, List<Long> ids) {
        Map<Long, Long> requestedCounts = countRequestedIds(eventId, ids);
        Map<Long, SectorByBatch> recordsById =
                loadAndValidateRecordsForEvent(eventId, requestedCounts.keySet());

        for (Map.Entry<Long, Long> request : requestedCounts.entrySet()) {
            int amount;
            try {
                amount = Math.toIntExact(request.getValue());
            } catch (ArithmeticException ex) {
                throw new InvalidPriceLookupException(
                        "La cantidad solicitada excede el rango soportado.");
            }

            SectorByBatch record = recordsById.get(request.getKey());
            if (record.getQuantity() < amount) {
                throw new InsufficientStockException(request.getKey());
            }
            if (precioRepository.decrementQuantityIfAvailable(request.getKey(), amount) != 1) {
                throw new InsufficientStockException(request.getKey());
            }
        }

        List<SectorByBatch> updatedRecords = repository.findAllById(requestedCounts.keySet());
        Map<Long, SectorByBatch> updatedRecordsById = new LinkedHashMap<>();
        for (SectorByBatch record : updatedRecords) {
            updatedRecordsById.put(record.getId(), record);
        }
        return requestedCounts.keySet().stream()
                .map(updatedRecordsById::get)
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('Cliente')")
    public void restoreStockForEvent(Long eventId, List<Long> ids) {
        Map<Long, Long> requestedCounts = countRequestedIds(eventId, ids);
        loadAndValidateRecordsForEvent(eventId, requestedCounts.keySet());

        for (Map.Entry<Long, Long> request : requestedCounts.entrySet()) {
            int amount;
            try {
                amount = Math.toIntExact(request.getValue());
            } catch (ArithmeticException ex) {
                throw new InvalidPriceLookupException(
                        "La cantidad a restaurar excede el rango soportado.");
            }

            if (precioRepository.restoreQuantity(request.getKey(), amount) != 1) {
                throw new ResourceNotFoundException(request.getKey());
            }
        }
    }

    private Map<Long, Long> countRequestedIds(Long eventId, List<Long> ids) {
        if (eventId == null || eventId <= 0) {
            throw new InvalidPriceLookupException("eventId debe ser un entero positivo.");
        }
        if (ids == null || ids.isEmpty()) {
            throw new InvalidPriceLookupException("Debe enviarse al menos un ID de precio.");
        }

        Map<Long, Long> requestedCounts = new LinkedHashMap<>();
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new InvalidPriceLookupException(
                        "Cada ID de precio debe ser un entero positivo.");
            }
            requestedCounts.merge(id, 1L, Math::addExact);
        }
        return requestedCounts;
    }

    private Map<Long, SectorByBatch> loadAndValidateRecordsForEvent(
            Long eventId,
            Set<Long> ids) {
        List<SectorByBatch> records = repository.findAllById(ids);
        Map<Long, SectorByBatch> recordsById = new LinkedHashMap<>();
        for (SectorByBatch record : records) {
            recordsById.put(record.getId(), record);
        }

        for (Long id : ids) {
            SectorByBatch record = recordsById.get(id);
            if (record == null) {
                throw new ResourceNotFoundException(id);
            }
            if (record.getPrice() < 1
                    || record.getSector() == null
                    || record.getSector().getEvent() == null
                    || record.getBatch() == null
                    || record.getBatch().getEvent() == null) {
                throw new InvalidPriceLookupException(
                        "El precio " + id + " no tiene datos válidos.");
            }
            if (!eventId.equals(record.getSector().getEvent().getId())
                    || !eventId.equals(record.getBatch().getEvent().getId())) {
                throw new InvalidPriceLookupException(
                        "El precio " + id + " no pertenece al evento " + eventId + ".");
            }
        }
        return recordsById;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')") // Todos pueden acceder a la información
    public PageResult<SectorByBatchDto> getByFilter(PrecioFilter filter) {
        PageResult<SectorByBatchDto> pageResult = super.getByFilter(filter);
        Cache cache = cacheManager.getCache(RedisCacheConfig.CACHE_SD_EVENT_PRECIO);

        if (cache != null && pageResult.items() != null) {
            for (SectorByBatchDto dto : pageResult.items()) {
                String key = "api_precio_" + dto.getId();
                cache.put(key, dto);
                log.info("Elemento cacheado individualmente tras búsqueda masiva: {}", key);
            }
        }
        return pageResult;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')") // Todos pueden leer
    @Cacheable(value= RedisCacheConfig.CACHE_SD_EVENT_PRECIO, key="'api_precio_' + #id")
    public SectorByBatchDto getById(Long id){
        log.info("Cache Miss: No se encontró api_precio_{} en Redis. Consultando DB y guardando en caché.", id);
        return super.getById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('Administrador')") // Solo el Administrador puede borrar
    @CacheEvict(value = RedisCacheConfig.CACHE_SD_EVENT_PRECIO, key="'api_precio_' + #id")
    public boolean delete(Long id) {
        log.info("Invalidando caché y eliminando de DB el registro api_precio_{}.", id);
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Administrador', 'Manager')") // Administradores y Managers pueden editar
    @CachePut(value = RedisCacheConfig.CACHE_SD_EVENT_PRECIO, key="'api_precio_' + #id")
    public SectorByBatchDto update(Long id, UpdateSectorByBatchDto updateDto) {
        log.info("Actualizando DB y sobrescribiendo caché para api_precio_{}.", id);
        return super.update(id, updateDto);
    }
}
