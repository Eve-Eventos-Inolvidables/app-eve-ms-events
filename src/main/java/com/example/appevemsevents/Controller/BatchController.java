package com.example.appevemsevents.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Models.Event.Batch.Batch;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsevents.Service.Batch.BatchFilter;
import com.example.appevemsevents.Service.Batch.BatchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.BatchDto;
import org.openapitools.model.UpdateBatchDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/batch")
public class BatchController extends BaseController<Batch, BatchDto, BatchFilter, BatchService> {

    protected BatchController(BatchService service){
        super(service);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Administrador', 'Manager')") // Modificar tandas
    public Response<BatchDto> update(@PathVariable Long id, @Valid @RequestBody UpdateBatchDto dto){
        log.info("Se solicitó actualizar la tanda con id {} (nuevos valores: startDate:{}, endDate:{}, eventId:{}, Nombre:{})",
                id, dto.getStartDate(), dto.getEndDate(), dto.getEventId(), dto.getName());
        return ResponseFactory.ok("Tanda actualizada correctamente", service.update(id, dto));
    }
}