package com.example.appevemsevents.Service.Batch;

import com.example.appevecommon.Models.Event.Batch.Batch;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;
import com.example.appevecommon.Service.Utilities.Responses.Ok.PageResult;
import com.example.appevemsevents.Config.RedisCacheConfig;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.BatchDto;
import org.openapitools.model.UpdateBatchDto;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BatchService extends UpdatableService<Batch, BatchDto, UpdateBatchDto, BatchMapper, BatchFilter> {

    private final CacheManager cacheManager;

    protected BatchService(IBaseRepository<Batch> repository, BatchMapper mapper, CacheManager cacheManager){
        super(repository, mapper);
        this.cacheManager = cacheManager;
    }

    @Override
    public Specification<Batch> toSpecification(BatchFilter filter){
        return (root, query, criteriaBuilder) -> {
            if(filter == null) return criteriaBuilder.conjunction();
            List<Predicate> predicateList = new ArrayList<>();
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')") // Todos pueden acceder a la información
    public PageResult<BatchDto> getByFilter(BatchFilter filter) {
        PageResult<BatchDto> pageResult = super.getByFilter(filter);
        Cache cache = cacheManager.getCache(RedisCacheConfig.CACHE_SD_EVENT_BATCH);

        if (cache != null && pageResult.items() != null) {
            for (BatchDto dto : pageResult.items()) {
                String key = "api_batch_" + dto.getId();
                cache.put(key, dto);
                log.info("Elemento cacheado individualmente tras búsqueda masiva: {}", key);
            }
        }
        return pageResult;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')") // Todos pueden leer
    @Cacheable(value= RedisCacheConfig.CACHE_SD_EVENT_BATCH, key="'api_batch_' + #id")
    public BatchDto getById(Long id){
        log.info("Cache Miss: No se encontró api_batch_{} en Redis. Consultando DB y guardando en caché.", id);
        return super.getById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('Administrador')") // Solo el Administrador puede borrar
    @CacheEvict(value = RedisCacheConfig.CACHE_SD_EVENT_BATCH, key="'api_batch_' + #id")
    public boolean delete(Long id) {
        log.info("Invalidando caché y eliminando de DB el registro api_batch_{}.", id);
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Administrador', 'Manager')") // Administradores y Managers pueden editar
    @CachePut(value = RedisCacheConfig.CACHE_SD_EVENT_BATCH, key="'api_batch_' + #id")
    public BatchDto update(Long id, UpdateBatchDto updateDto) {
        log.info("Actualizando DB y sobrescribiendo caché para api_batch_{}.", id);
        return super.update(id, updateDto);
    }
}