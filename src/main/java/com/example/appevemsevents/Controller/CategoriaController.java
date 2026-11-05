package com.example.appevemsevents.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Models.Event.Addons.EventCategory;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsevents.Service.Categoria.CategoriaFilter;
import com.example.appevemsevents.Service.Categoria.CategoriaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.EventCategoryDto;
import org.openapitools.model.UpdateEventCategoryDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/categoria")
public class CategoriaController extends BaseController<EventCategory, EventCategoryDto, CategoriaFilter, CategoriaService> {

    protected CategoriaController(CategoriaService service){
        super(service);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Administrador', 'Manager')") // Modificar categorías
    public Response<EventCategoryDto> update(@PathVariable Long id, @Valid @RequestBody UpdateEventCategoryDto dto){
        log.info("Se solicitó actualizar la categoría con id {} (nuevos valores: nombre:{})",
                id, dto.getName());
        return ResponseFactory.ok("Categoría actualizada correctamente", service.update(id, dto));
    }
}