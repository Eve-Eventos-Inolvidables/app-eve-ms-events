package com.example.appevemsevents.Service.Batch;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.model.BatchDto;
import org.openapitools.model.CreateBatchDto;
import org.openapitools.model.UpdateBatchDto;

import com.example.appevecommon.Models.Event.Batch.Batch;
import com.example.appevecommon.Service.Utilities.UpdatableMapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class BatchMapper implements UpdatableMapper<Batch, BatchDto, UpdateBatchDto> {

    @Mapping(source = "event.id", target = "eventId")
    public abstract BatchDto toDto(Batch entity);

    @Mapping(source = "eventId", target = "event.id")
    public abstract Batch toEntity(CreateBatchDto dto);
}
