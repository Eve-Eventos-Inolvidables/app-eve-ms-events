package com.example.appevemsevents.Service.Precio;

import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Models.Event.Sector.Sector;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;
import jakarta.persistence.criteria.Predicate;
import org.openapitools.model.CreateSectorByBatchDto;
import org.openapitools.model.SectorByBatchDto;
import org.openapitools.model.UpdateSectorByBatchDto;
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
}
