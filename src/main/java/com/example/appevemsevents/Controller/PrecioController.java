package com.example.appevemsevents.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsevents.Service.Precio.PrecioFilter;
import com.example.appevemsevents.Service.Precio.PrecioService;
import jakarta.validation.Valid;
import org.openapitools.model.CreateSectorByBatchDto;
import org.openapitools.model.SectorByBatchDto;
import org.openapitools.model.UpdateSectorByBatchDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/precio")
public class PrecioController extends BaseController<SectorByBatch, SectorByBatchDto, PrecioFilter, PrecioService> {
    protected PrecioController(PrecioService service){
        super(service);
    }
    @PostMapping
    Response<SectorByBatchDto> create(@Valid @RequestBody CreateSectorByBatchDto dto){
        return ResponseFactory.ok(service.createPrecio(dto));
    }
    @GetMapping("/all")
    Response<List<SectorByBatchDto>> getAll(){
        return ResponseFactory.ok(service.getAll());
    }
    @PatchMapping("/id")
    Response<SectorByBatchDto> update(@PathVariable Long id, @Valid @RequestBody
    UpdateSectorByBatchDto dto){
        return ResponseFactory.ok("Precio actualizado correctamente",
                service.update(id,dto));
    }
}
