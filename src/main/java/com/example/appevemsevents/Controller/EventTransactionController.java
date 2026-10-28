package com.example.appevemsevents.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsevents.Service.EventTransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CreateEventTransDto;
import org.openapitools.model.SimpleEventDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/event")
public class EventTransactionController {
    private final EventTransactionService eventTransactionService;

    public EventTransactionController(EventTransactionService eventTransactionService) {
        this.eventTransactionService = eventTransactionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('Administrador')")// Solo el Administrador puede crear eventos nuevos
    public Response<SimpleEventDto> createEvent(@Valid @RequestBody CreateEventTransDto dto){
        log.info("Se solicitó crear un evento completo de nombre {} para la fecha {} con límite de tickets por persona de {}",
                dto.getName(),
                dto.getDate(),
                dto.getTicketsPerPersonLimit());
        return ResponseFactory.ok(eventTransactionService.createCompleteEvent(dto));
    }
}