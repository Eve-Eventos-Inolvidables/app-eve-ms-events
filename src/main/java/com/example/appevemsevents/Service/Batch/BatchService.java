package com.example.appevemsevents.Service.Batch;

import com.example.appevecommon.Models.Event.Batch.Batch;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;
import jakarta.persistence.criteria.Predicate;
import org.openapitools.model.BatchDto;
import org.openapitools.model.UpdateBatchDto;
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

}
