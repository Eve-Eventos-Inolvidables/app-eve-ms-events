package com.example.appevemsevents.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Models.Event.Sector.Sector;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsevents.Service.Sector.SectorFilter;
import com.example.appevemsevents.Service.Sector.SectorService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.SectorDto;
import org.openapitools.model.UpdateSectorDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/sector")
public class SectorController extends BaseController<Sector, SectorDto, SectorFilter, SectorService> {

    protected SectorController(SectorService service){
        super(service);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Administrador', 'Manager')") // Modificar sectores
    public Response<SectorDto> update(@PathVariable Long id, @Valid @RequestBody UpdateSectorDto dto){
        log.info("Se solicitó actualizar el sector con id {} (nuevos valores: capacidad:{}, eventoID:{}, Nombre:{})",
                id, dto.getCapacity(), dto.getEventId(), dto.getName());
        return ResponseFactory.ok("Sector actualizado correctamente", service.update(id, dto));
    }
}