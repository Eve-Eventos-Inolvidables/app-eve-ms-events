package com.example.appevemsevents.Service.Sector;

import com.example.appevecommon.Models.Event.Sector.Sector;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;

import jakarta.persistence.criteria.Predicate;
import org.openapitools.model.SectorDto;
import org.openapitools.model.UpdateSectorDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.appevemsevents.Config.RedisCacheConfig;

import java.util.ArrayList;
import java.util.List;
@Service
public class SectorService extends UpdatableService<Sector, SectorDto, UpdateSectorDto,SectorMapper,SectorFilter> {
    protected SectorService(IBaseRepository<Sector> repository, SectorMapper mapper){super (repository,mapper);}
    @Override
    public Specification<Sector> toSpecification (SectorFilter filter){
        return (root, query, criteriaBuilder) -> {
            if(filter == null) return criteriaBuilder.conjunction(); // Retorna 1=1 (true) si no hay filtro
            List<Predicate> predicateList = new ArrayList<>();
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
    }
    @Override
    @Cacheable(value= RedisCacheConfig.CACHE_SD_EVENT_SECTOR,key="'api_sector' + #id")
    public SectorDto getById(Long id){
        return super.getById(id);
    }
}
