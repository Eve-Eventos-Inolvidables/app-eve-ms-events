package com.example.appevemsevents.Service.Categoria;

import com.example.appevecommon.Models.Event.Addons.EventCategory;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;
import com.example.appevecommon.Service.Utilities.Responses.Ok.PageResult;
import com.example.appevemsevents.Config.RedisCacheConfig;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.EventCategoryDto;
import org.openapitools.model.UpdateEventCategoryDto;
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
public class CategoriaService extends UpdatableService<EventCategory, EventCategoryDto, UpdateEventCategoryDto, CategoriaMapper, CategoriaFilter> {

    private final CacheManager cacheManager;

    protected CategoriaService(IBaseRepository<EventCategory> repository, CategoriaMapper mapper, CacheManager cacheManager) {
        super(repository, mapper);
        this.cacheManager = cacheManager;
    }

    @Override
    public Specification<EventCategory> toSpecification(CategoriaFilter filter) {
        return (root, query, criteriaBuilder) -> {
            if(filter == null) return criteriaBuilder.conjunction();
            List<Predicate> predicateList = new ArrayList<>();
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')")
    public PageResult<EventCategoryDto> getByFilter(CategoriaFilter filter) {
        PageResult<EventCategoryDto> pageResult = super.getByFilter(filter);
        Cache cache = cacheManager.getCache(RedisCacheConfig.CACHE_SD_EVENT_CATEGORIA);

        if (cache != null && pageResult.items() != null) {
            for (EventCategoryDto dto : pageResult.items()) {
                String key = "api_categoria_" + dto.getId();
                cache.put(key, dto);
                log.info("Elemento cacheado individualmente tras búsqueda masiva: {}", key);
            }
        }
        return pageResult;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Cliente', 'Administrador', 'Manager')")
    @Cacheable(value = RedisCacheConfig.CACHE_SD_EVENT_CATEGORIA, key = "'api_categoria_' + #id")
    public EventCategoryDto getById(Long id) {
        log.info("Cache Miss: No se encontró api_categoria_{} en Redis. Consultando DB y guardando en caché.", id);
        return super.getById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('Administrador')")
    @CacheEvict(value = RedisCacheConfig.CACHE_SD_EVENT_CATEGORIA, key = "'api_categoria_' + #id")
    public boolean delete(Long id) {
        log.info("Invalidando caché y eliminando de DB el registro api_categoria_{}.", id);
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('Administrador', 'Manager')")
    @CachePut(value = RedisCacheConfig.CACHE_SD_EVENT_CATEGORIA, key = "'api_categoria_' + #id")
    public EventCategoryDto update(Long id, UpdateEventCategoryDto updateDto) {
        log.info("Actualizando DB y sobrescribiendo caché para api_categoria_{}.", id);
        return super.update(id, updateDto);
    }
}