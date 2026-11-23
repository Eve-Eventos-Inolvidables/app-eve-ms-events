package com.example.appevemsevents.Service.Precio;

import com.example.appevecommon.Models.Event.Batch.Batch;
import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Models.Event.SimpleEvent;
import com.example.appevecommon.Models.Event.Sector.Sector;
import com.example.appevecommon.Repository.IBaseRepository;
import com.example.appevecommon.Service.Exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.SectorByBatchDto;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

class PrecioServiceTest {

    private IBaseRepository<SectorByBatch> repository;
    private PrecioMapper mapper;
    private PrecioService service;

    @BeforeEach
    void setUp() {
        repository = mock(IBaseRepository.class);
        mapper = mock(PrecioMapper.class);
        CacheManager cacheManager = mock(CacheManager.class);
        service = new PrecioService(repository, mapper, cacheManager);
    }

    @Test
    void getByIdsForEventLoadsUniqueIdsOnceAndReturnsRequestedOrder() {
        SectorByBatch ten = sectorByBatch(10L, 100, 42L);
        SectorByBatch twenty = sectorByBatch(20L, 200, 42L);
        SectorByBatchDto tenDto = dto(10L);
        SectorByBatchDto twentyDto = dto(20L);

        when(repository.findAllById(anyIterable())).thenReturn(List.of(ten, twenty));
        when(mapper.toDto(ten)).thenReturn(tenDto);
        when(mapper.toDto(twenty)).thenReturn(twentyDto);

        List<SectorByBatchDto> result =
                service.getByIdsForEvent(42L, List.of(20L, 10L, 20L));

        assertEquals(List.of(20L, 10L),
                result.stream().map(SectorByBatchDto::getId).toList());
        verify(repository, times(1)).findAllById(anyIterable());
    }

    @Test
    void getByIdsForEventRejectsMissingOrArchivedIds() {
        when(repository.findAllById(anyIterable())).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getByIdsForEvent(42L, List.of(404L)));
    }

    @Test
    void getByIdsForEventRejectsPricesOutsideTheRequestedEvent() {
        when(repository.findAllById(anyIterable()))
                .thenReturn(List.of(sectorByBatch(10L, 100, 99L)));

        assertThrows(InvalidPriceLookupException.class,
                () -> service.getByIdsForEvent(42L, List.of(10L)));
    }

    @Test
    void getByIdsForEventRejectsEmptyOrInvalidSelectionsBeforeQuerying() {
        assertThrows(InvalidPriceLookupException.class,
                () -> service.getByIdsForEvent(42L, List.of()));
        assertThrows(InvalidPriceLookupException.class,
                () -> service.getByIdsForEvent(42L, List.of(10L, 0L)));

        verifyNoInteractions(repository);
    }

    private SectorByBatch sectorByBatch(long id, int price, long eventId) {
        SimpleEvent event = new SimpleEvent();
        event.setId(eventId);

        Sector sector = new Sector();
        sector.setEvent(event);

        Batch batch = new Batch();
        batch.setEvent(event);

        SectorByBatch record = new SectorByBatch();
        record.setId(id);
        record.setPrice(price);
        record.setSector(sector);
        record.setBatch(batch);
        return record;
    }

    private SectorByBatchDto dto(long id) {
        SectorByBatchDto dto = new SectorByBatchDto();
        dto.setId(id);
        return dto;
    }
}
