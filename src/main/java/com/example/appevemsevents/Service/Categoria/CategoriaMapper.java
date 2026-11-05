package com.example.appevemsevents.Service.Categoria;

import com.example.appevecommon.Models.Event.Addons.EventCategory;
import com.example.appevecommon.Service.Utilities.UpdatableMapper;
import org.mapstruct.Mapper;
import org.openapitools.model.CreateEventCategoryDto;
import org.openapitools.model.EventCategoryDto;
import org.openapitools.model.UpdateEventCategoryDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class CategoriaMapper implements UpdatableMapper<EventCategory, EventCategoryDto, UpdateEventCategoryDto> {
    public abstract EventCategory toEntity(CreateEventCategoryDto dto);

    public abstract EventCategoryDto toDto(EventCategory entity);
}
