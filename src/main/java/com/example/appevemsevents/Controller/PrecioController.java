package com.example.appevemsevents.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Service.Utilities.Responses.Error.ErrorResponse;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsevents.Service.Precio.InvalidPriceLookupException;
import com.example.appevemsevents.Service.Precio.PrecioFilter;
import com.example.appevemsevents.Service.Precio.PrecioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.SectorByBatchDto;
import org.openapitools.model.UpdateSectorByBatchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/precio")
public class PrecioController extends BaseController<SectorByBatch, SectorByBatchDto, PrecioFilter, PrecioService> {

    protected PrecioController(PrecioService service) {
        super(service);
    }

    // Por el momento no tiene sentido crear un nuevo precio, implica manejar stock de entradas en el evento global, etc.

    @PostMapping("/lookup")
    public Response<List<SectorByBatchDto>> getByIdsForEvent(
            @RequestParam(required = false) Long eventId,
            @RequestBody List<Long> ids) {
        log.info("Se solicitó consultar {} precios para el evento {}",
                ids == null ? 0 : ids.size(), eventId);
        return ResponseFactory.ok(
                "Precios consultados correctamente",
                service.getByIdsForEvent(eventId, ids));
    }

    @ExceptionHandler(InvalidPriceLookupException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPriceLookup(InvalidPriceLookupException ex) {
        return ResponseFactory.badRequest("Consulta de precios inválida", ex.getMessage());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Administrador', 'Manager')") // Modificar precios
    public Response<SectorByBatchDto> update(@PathVariable Long id, @Valid @RequestBody UpdateSectorByBatchDto dto){
        log.info("Se solicitó actualizar el precio con id {} (nuevo precio: {}, nueva cantidad: {})",
                id,
                dto.getPrice(),
                dto.getQuantity());
        return ResponseFactory.ok("Precio actualizado correctamente",
                service.update(id,dto));
    }
}
