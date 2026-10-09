package com.example.appevemsevents.Service.EventSimple;

import java.util.ArrayList;

import org.openapitools.model.CreateSimpleEventDto;
import org.openapitools.model.SimpleEventDto;
import org.openapitools.model.UpdateSimpleEventDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.appevecommon.Models.Event.SimpleEvent;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.UpdatableService;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

@Service
public class EventSimpleService extends
        UpdatableService<SimpleEvent, SimpleEventDto, UpdateSimpleEventDto, EventSimpleMapper, EventSimpleFilter> {
    
    // Constructor
            protected EventSimpleService(IBaseRepository<SimpleEvent> repository, EventSimpleMapper mapper) {
        super(repository, mapper);
    }

    // para crear un simpleevent
    public SimpleEventDto createSimpleEvent(CreateSimpleEventDto dto){
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    // getAll
    @Override 
    public List<SimpleEventDto> getAll(){
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    // para realizar filtros, con paginación y busquedas con el campo de name, id de categoria, fecha de inicio de visible para el evento
    @Override
    public Specification<SimpleEvent> toSpecification(EventSimpleFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) return cb.conjunction();
            
            List<Predicate> predicates = new ArrayList<>();
            
            // 1. Filtro por nombre
            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%")
                );
            }
            
            // 2. Filtro por ID de categoría 
            if (filter.getCategoryId() != null) {
                
                predicates.add(cb.equal(root.get("eventCategory").get("id"), filter.getCategoryId()));
            }
            
            // 3. Filtro por fecha de inicio visible
            if (filter.getVisibleStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("visibleStart"), filter.getVisibleStart()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
