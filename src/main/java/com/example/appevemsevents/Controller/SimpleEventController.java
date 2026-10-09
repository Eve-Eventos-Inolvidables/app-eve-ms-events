package com.example.appevemsevents.Controller;

import org.openapitools.model.CreateSimpleEventDto;
import org.openapitools.model.SimpleEventDto;
import org.openapitools.model.UpdateSimpleEventDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.appevemsevents.Service.EventSimple.EventSimpleFilter;
import com.example.appevemsevents.Service.EventSimple.EventSimpleService;
import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Models.Event.SimpleEvent;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;

import jakarta.validation.Valid;

import java.util.List;

@RestController 
@RequestMapping ("/api/sevent")
public class SimpleEventController extends BaseController<SimpleEvent,SimpleEventDto,EventSimpleFilter,EventSimpleService> {
    protected SimpleEventController(EventSimpleService service){
        super(service);
    }
    @PostMapping 
    Response<SimpleEventDto> create(@Valid @RequestBody CreateSimpleEventDto dto ){
        System.out.println(dto);
        return ResponseFactory.ok(service.createSimpleEvent(dto));
    }
    @GetMapping ("/all")
    Response<List<SimpleEventDto>> getAll(){
        return ResponseFactory.ok(service.getAll());
    }
    @PatchMapping ("/{id}")
    Response<SimpleEventDto> update(@PathVariable Long id, @Valid @RequestBody UpdateSimpleEventDto dto){
        return ResponseFactory.ok("Simple Event actualizado correctamente",service.update(id,dto));
    }
}
