package com.example.appevemsevents.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Service.Utilities.Responses.Error.ErrorResponse;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsevents.Service.Precio.InvalidPriceLookupException;
import com.example.appevemsevents.Service.Precio.InsufficientStockException;
import com.example.appevemsevents.Service.Precio.PrecioFilter;
import com.example.appevemsevents.Service.Precio.PrecioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.SectorByBatchDto;
import org.openapitools.model.UpdateSectorByBatchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/precio")
public class PrecioController extends BaseController<SectorByBatch, SectorByBatchDto, PrecioFilter, PrecioService> {

    private final String salesStockCommandKey;

    protected PrecioController(
            PrecioService service,
            @Value("${app.sales.stock-command-key:}") String salesStockCommandKey) {
        super(service);
        this.salesStockCommandKey = salesStockCommandKey;
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

    @PostMapping("/consume")
    @PreAuthorize("hasAuthority('Cliente')")
    public Response<List<SectorByBatchDto>> consumeStock(
            @RequestParam(required = false) Long eventId,
            @RequestBody List<Long> ids,
            @RequestHeader(value = "X-Sales-Stock-Key", required = false) String serviceKey) {
        verifySalesStockCommandKey(serviceKey);
        return ResponseFactory.ok(
                "Stock descontado correctamente",
                service.consumeStockForEvent(eventId, ids));
    }

    @PostMapping("/release")
    @PreAuthorize("hasAuthority('Cliente')")
    public Response<Void> releaseStock(
            @RequestParam(required = false) Long eventId,
            @RequestBody List<Long> ids,
            @RequestHeader(value = "X-Sales-Stock-Key", required = false) String serviceKey) {
        verifySalesStockCommandKey(serviceKey);
        service.restoreStockForEvent(eventId, ids);
        return ResponseFactory.ok("Stock restaurado correctamente", null);
    }

    @ExceptionHandler(InvalidPriceLookupException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPriceLookup(InvalidPriceLookupException ex) {
        return ResponseFactory.badRequest("Consulta de precios inválida", ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseFactory.error(409, "Stock insuficiente", ex.getMessage());
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

    private void verifySalesStockCommandKey(String suppliedKey) {
        if (salesStockCommandKey.isBlank()
                || suppliedKey == null
                || !MessageDigest.isEqual(
                        salesStockCommandKey.getBytes(StandardCharsets.UTF_8),
                        suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("Solo Sales puede modificar el stock de precios.");
        }
    }
}
