package com.example.appevemsevents.Service.EventSimple;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.example.appevemsevents.Config.RedisCacheConfig;
import com.example.appevecommon.Models.Event.SimpleEvent;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;
import com.example.appevecommon.Service.Utilities.Responses.Ok.PageResult;
import org.openapitools.model.*;

@Slf4j
@Service
public class EventSimpleService extends
        UpdatableService<SimpleEvent, SimpleEventDto, UpdateSimpleEventDto, EventSimpleMapper, EventSimpleFilter> {

    private final CacheManager cacheManager;

    protected EventSimpleService(IBaseRepository<SimpleEvent> repository, EventSimpleMapper mapper, CacheManager cacheManager) {
        super(repository, mapper);
        this.cacheManager = cacheManager;
    }

    @Override
    public Specification<SimpleEvent> toSpecification(EventSimpleFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) return cb.conjunction();
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%")
                );
            }
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("eventCategory").get("id"), filter.getCategoryId()));
            }
            if (filter.getVisibleStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("visibleStart"), filter.getVisibleStart()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')") // Todos pueden acceder a la información
    public PageResult<SimpleEventDto> getByFilter(EventSimpleFilter filter) {
        PageResult<SimpleEventDto> pageResult = super.getByFilter(filter);
        Cache cache = cacheManager.getCache(RedisCacheConfig.CACHE_SD_EVENT);

        if (cache != null && pageResult.items() != null) {
            for (SimpleEventDto dto : pageResult.items()) {
                String key = "api_event_" + dto.getId();
                cache.put(key, dto);
                log.info("Elemento cacheado individualmente tras búsqueda masiva: {}", key);
            }
        }
        return pageResult;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')") // Todos pueden leer
    @Cacheable(value= RedisCacheConfig.CACHE_SD_EVENT, key="'api_event_' + #id")
    public SimpleEventDto getById(Long id){
        log.info("Cache Miss: No se encontró api_event_{} en Redis. Consultando DB y guardando en caché.", id);
        return super.getById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('Administrador')") // Solo el Administrador puede borrar
    @CacheEvict(value = RedisCacheConfig.CACHE_SD_EVENT, key="'api_event_' + #id")
    public boolean delete(Long id) {
        log.info("Invalidando caché y eliminando de DB el registro api_event_{}.", id);
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Administrador', 'Manager')") // Administradores y Managers pueden editar
    @CachePut(value = RedisCacheConfig.CACHE_SD_EVENT, key="'api_event_' + #id")
    public SimpleEventDto update(Long id, UpdateSimpleEventDto updateDto) {
        log.info("Actualizando DB y sobrescribiendo caché para api_event_{}.", id);
        return super.update(id, updateDto);
    }
}