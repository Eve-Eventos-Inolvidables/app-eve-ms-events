package com.example.appevemsevents.Service.Sector;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.model.CreateSectorDto;
import org.openapitools.model.SectorDto;
import org.openapitools.model.UpdateSectorDto;

import com.example.appevecommon.Models.Event.Sector.Sector;
import com.example.appevecommon.Service.Utilities.UpdatableMapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class SectorMapper implements UpdatableMapper<Sector, SectorDto, UpdateSectorDto> {

    @Mapping(source = "event.id", target = "eventId")
    public abstract SectorDto toDto(Sector entity);

    @Mapping(source = "eventId", target = "event.id")
    public abstract Sector toEntity(CreateSectorDto dto);
}
