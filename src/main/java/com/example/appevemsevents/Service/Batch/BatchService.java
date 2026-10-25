package com.example.appevemsevents.Service.Batch;

import com.example.appevecommon.Models.Event.Batch.Batch;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;
import com.example.appevemsevents.Config.RedisCacheConfig;
import jakarta.persistence.criteria.Predicate;
import org.openapitools.model.BatchDto;
import org.openapitools.model.SectorDto;
import org.openapitools.model.UpdateBatchDto;
import org.openapitools.model.UpdateSectorDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatchService extends UpdatableService<Batch, BatchDto, UpdateBatchDto,BatchMapper,BatchFilter> {
    protected BatchService(IBaseRepository<Batch> repository,BatchMapper mapper){
        super(repository,mapper);
    }

    @Override
    public Specification<Batch> toSpecification(BatchFilter filter){
        return (root, query, criteriaBuilder) -> {
            if(filter == null) return criteriaBuilder.conjunction(); // Retorna 1=1 (true) si no hay filtro
            List<Predicate> predicateList = new ArrayList<>();
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
    }

    @Override
    @Cacheable(value= RedisCacheConfig.CACHE_SD_EVENT_BATCH,key="'api_batch' + #id")
    public BatchDto getById(Long id){
        return super.getById(id);
    }

    @Override
    @CacheEvict(value = RedisCacheConfig.CACHE_SD_EVENT_BATCH, key="'api_batch' + #id")
    public boolean delete(Long id) {
        return super.delete(id);
    }
    @Override
    @CachePut(value = RedisCacheConfig.CACHE_SD_EVENT_BATCH, key="'api_batch' + #id")
    public BatchDto update(Long id, UpdateBatchDto updateDto) {
        return super.update(id, updateDto);
    }
}
