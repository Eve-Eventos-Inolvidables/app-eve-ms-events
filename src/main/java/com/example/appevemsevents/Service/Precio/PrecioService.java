package com.example.appevemsevents.Service.Precio;

import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Models.Event.Sector.Sector;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;
import com.example.appevemsevents.Config.RedisCacheConfig;
import jakarta.persistence.criteria.Predicate;
import org.openapitools.model.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrecioService extends UpdatableService<SectorByBatch, SectorByBatchDto, UpdateSectorByBatchDto,PrecioMapper,PrecioFilter> {
    protected PrecioService(IBaseRepository<SectorByBatch> repository,PrecioMapper mapper){
        super(repository,mapper);
    }

    @Override
    public List<SectorByBatchDto> getAll(){return repository.findAll().stream().map(mapper::toDto).toList();}

    @Override
    public Specification<SectorByBatch> toSpecification(PrecioFilter filter) {
        return (root, query, criteriaBuilder) -> {
            if(filter == null) return criteriaBuilder.conjunction(); // Retorna 1=1 (true) si no hay filtro
            List<Predicate> predicateList = new ArrayList<>();
            if(filter.getPrice() != null) {
                predicateList.add(
                        criteriaBuilder.equal(root.get("price"), filter.getPrice())
                );
            }

            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
    }

    @Override
    @Cacheable(value= RedisCacheConfig.CACHE_SD_EVENT_PRECIO,key="'api_precio' + #id")
    public SectorByBatchDto getById(Long id){
        return super.getById(id);
    }

    @Override
    @CacheEvict(value = RedisCacheConfig.CACHE_SD_EVENT_PRECIO, key="'api_precio' + #id")
    public boolean delete(Long id) {
        return super.delete(id);
    }
    @Override
    @CachePut(value = RedisCacheConfig.CACHE_SD_EVENT_PRECIO, key="'api_precio' + #id")
    public SectorByBatchDto update(Long id, UpdateSectorByBatchDto updateDto) {
        return super.update(id, updateDto);
    }
}
