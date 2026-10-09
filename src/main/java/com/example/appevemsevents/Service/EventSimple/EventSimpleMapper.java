package com.example.appevemsevents.Service.EventSimple;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.model.CreateSimpleEventDto;
import org.openapitools.model.SimpleEventDto;
import org.openapitools.model.UpdateSimpleEventDto;

import com.example.appevecommon.Service.Utilities.UpdatableMapper;
import com.example.appevecommon.Models.Event.SimpleEvent;
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class EventSimpleMapper implements UpdatableMapper<SimpleEvent,SimpleEventDto,UpdateSimpleEventDto> {

    @Mapping(source = "eventCategory.id", target = "categoryId")
    public abstract SimpleEventDto toDto(SimpleEvent entity);

    @Mapping(source = "categoryId", target = "eventCategory.id")
    public abstract SimpleEvent toEntity(CreateSimpleEventDto dto);

}
