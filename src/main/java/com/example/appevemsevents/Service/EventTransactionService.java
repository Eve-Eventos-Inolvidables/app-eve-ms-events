package com.example.appevemsevents.Service;

import com.example.appevecommon.Models.Event.Batch.Batch;
import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Models.Event.Sector.Sector;
import com.example.appevecommon.Models.Event.SimpleEvent;
import com.example.appevemsevents.Repository.IBatchRepository;
import com.example.appevemsevents.Repository.IEventRepository;
import com.example.appevemsevents.Repository.IPrecioRepository;
import com.example.appevemsevents.Repository.ISectorRepository;
import com.example.appevemsevents.Service.Batch.BatchMapper;
import com.example.appevemsevents.Service.EventSimple.EventSimpleMapper;
import com.example.appevemsevents.Service.Precio.PrecioMapper;
import com.example.appevemsevents.Service.Sector.SectorMapper;
import org.openapitools.model.CreateEventTransDto;
import org.openapitools.model.SimpleEventDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    public SimpleEventDto createCompleteEvent(CreateEventTransDto dto) {
        // 1. Crear y guardar el Evento (Agregado Raíz)
        SimpleEvent eventEntity = eventMapper.toEntityFromTrans(dto);
        SimpleEvent savedEvent = eventRepository.save(eventEntity);

        // 2. Crear y guardar los Sectores
        List<Sector> savedSectors = new ArrayList<>();
        if (dto.getSectores() != null) {
            dto.getSectores().forEach(sectorDto -> {
                Sector sector = sectorMapper.toEntity(sectorDto);
                sector.setEvent(savedEvent); // Vinculamos con el ID real del evento
                savedSectors.add(sectorRepository.save(sector));
            });
        }

        // 3. Crear y guardar las Tandas (Batches)
        List<Batch> savedBatches = new ArrayList<>();
        if (dto.getTandas() != null) {
            dto.getTandas().forEach(batchDto -> {
                Batch batch = batchMapper.toEntity(batchDto);
                batch.setEvent(savedEvent); // Vinculamos con el ID real del evento
                savedBatches.add(batchRepository.save(batch));
            });
        }

        // 4. Crear y guardar los Precios (SectorByBatch)
        if (dto.getPrecios() != null) {
            dto.getPrecios().forEach(precioDto -> {
                SectorByBatch price = precioMapper.toEntity(precioDto);

                /*
                 * Como el frontend aún no tiene los IDs reales de la base de datos,
                 * utiliza los campos sectorId y batchId del DTO como ÍNDICES de los arrays.
                 * Convertimos el Long que trae el DTO a int para buscar en las listas en memoria.
                 */
                int sectorIndex = precioDto.getSectorId().intValue();
                int batchIndex = precioDto.getBatchId().intValue();

                // Recuperamos las entidades ya guardadas (que ahora sí tienen IDs reales)
                Sector assignedSector = savedSectors.get(sectorIndex);
                Batch assignedBatch = savedBatches.get(batchIndex);

                // Asignamos las relaciones fuertes
                price.setSector(assignedSector);
                price.setBatch(assignedBatch);

                precioRepository.save(price);
            });
        }
        return eventMapper.toDto(savedEvent);
    }
    }



