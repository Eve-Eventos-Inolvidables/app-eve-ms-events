package com.example.appevemsevents.Service.Precio;

import com.example.appevecommon.Service.Utilities.UpdatableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.model.CreateSectorByBatchDto;
import org.openapitools.model.SectorByBatchDto;

import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import org.openapitools.model.UpdateSectorByBatchDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class PrecioMapper implements UpdatableMapper<SectorByBatch, SectorByBatchDto, UpdateSectorByBatchDto> {
    @Mapping(source = "sector.id", target = "sectorId")
    @Mapping(source = "batch.id", target = "batchId")
    public abstract SectorByBatchDto toDto(SectorByBatch entity);

    @Mapping(source = "sectorId", target = "sector.id")
    @Mapping(source = "batchId", target = "batch.id")
    public abstract SectorByBatch toEntity(CreateSectorByBatchDto dto);
}
