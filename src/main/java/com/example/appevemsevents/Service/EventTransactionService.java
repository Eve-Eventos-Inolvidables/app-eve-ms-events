package com.example.appevemsevents.Service;

import com.example.appevecommon.Models.Event.Batch.Batch;
import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Models.Event.Sector.Sector;
import com.example.appevecommon.Models.Event.SimpleEvent;
import com.example.appevemsevents.Config.RedisCacheConfig;
import com.example.appevemsevents.Repository.IBatchRepository;
import com.example.appevemsevents.Repository.IEventRepository;
import com.example.appevemsevents.Repository.IPrecioRepository;
import com.example.appevemsevents.Repository.ISectorRepository;
import com.example.appevemsevents.Service.Batch.BatchMapper;
import com.example.appevemsevents.Service.EventSimple.EventSimpleMapper;
import com.example.appevemsevents.Service.Precio.PrecioMapper;
import com.example.appevemsevents.Service.Sector.SectorMapper;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CreateEventTransDto;
import org.openapitools.model.SimpleEventDto;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EventTransactionService {
    private final IBatchRepository batchRepository;
    private final IEventRepository eventRepository;
    private final IPrecioRepository precioRepository;
    private final ISectorRepository sectorRepository;

    private final BatchMapper batchMapper;
    private final EventSimpleMapper eventMapper;
    private final PrecioMapper precioMapper;
    private final SectorMapper sectorMapper;

    public EventTransactionService(
            IEventRepository eventRepository,
            ISectorRepository sectorRepository,
            IBatchRepository batchRepository,
            IPrecioRepository precioRepository,
            BatchMapper batchMapper,
            EventSimpleMapper eventMapper,
            PrecioMapper precioMapper,
            SectorMapper sectorMapper
    ){
        this.eventRepository = eventRepository;
        this.sectorRepository = sectorRepository;
        this.batchRepository = batchRepository;
        this.precioRepository = precioRepository;
        this.batchMapper = batchMapper;
        this.eventMapper = eventMapper;
        this.precioMapper = precioMapper;
        this.sectorMapper = sectorMapper;
    }

    @Transactional
    // Estandarizado al nombre de caché y formato de key del resto del sistema
    @CachePut(value = RedisCacheConfig.CACHE_SD_EVENT, key = "'api_event_' + #result.id", condition = "#result.id != null")
    public SimpleEventDto createCompleteEvent(CreateEventTransDto dto) {
        log.info("Iniciando transacción para crear el evento complejo: {}", dto.getName());

        // 1. Crear y guardar el Evento (Agregado Raíz)
        SimpleEvent eventEntity = eventMapper.toEntityFromTrans(dto);
        SimpleEvent savedEvent = eventRepository.save(eventEntity);
        log.info("Evento principal creado en BD con ID: {}", savedEvent.getId());

        // 2. Crear y guardar los Sectores
        List<Sector> savedSectors = new ArrayList<>();
        if (dto.getSectores() != null) {
            dto.getSectores().forEach(sectorDto -> {
                Sector sector = sectorMapper.toEntity(sectorDto);
                sector.setEvent(savedEvent); // Vinculamos con el ID real del evento
                savedSectors.add(sectorRepository.save(sector));
            });
            log.info("Se guardaron {} sectores para el evento ID: {}", savedSectors.size(), savedEvent.getId());
        }

        // 3. Crear y guardar las Tandas (Batches)
        List<Batch> savedBatches = new ArrayList<>();
        if (dto.getTandas() != null) {
            dto.getTandas().forEach(batchDto -> {
                Batch batch = batchMapper.toEntity(batchDto);
                batch.setEvent(savedEvent); // Vinculamos con el ID real del evento
                savedBatches.add(batchRepository.save(batch));
            });
            log.info("Se guardaron {} tandas para el evento ID: {}", savedBatches.size(), savedEvent.getId());
        }

        // 4. Crear y guardar los Precios (SectorByBatch)
        if (dto.getPrecios() != null) {
            dto.getPrecios().forEach(precioDto -> {
                SectorByBatch price = precioMapper.toEntity(precioDto);

                int sectorIndex = precioDto.getSectorId().intValue();
                int batchIndex = precioDto.getBatchId().intValue();

                Sector assignedSector = savedSectors.get(sectorIndex);
                Batch assignedBatch = savedBatches.get(batchIndex);

                price.setSector(assignedSector);
                price.setBatch(assignedBatch);

                precioRepository.save(price);
            });
            log.info("Se generaron y vincularon {} combinaciones de precios (Sector x Tanda)", dto.getPrecios().size());
        }

        log.info("Transacción completada exitosamente. Evento {} (ID: {}) guardado en caché.", savedEvent.getName(), savedEvent.getId());
        return eventMapper.toDto(savedEvent);
    }
}