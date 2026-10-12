package com.example.appevemsevents.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsevents.Service.EventTransactionService;
import jakarta.validation.Valid;
import org.openapitools.model.CreateEventTransDto;
import org.openapitools.model.ResponseDto;
import org.openapitools.model.SimpleEventDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/event")
public class EventTransactionController {
    private final EventTransactionService eventTransactionService;

    public EventTransactionController(EventTransactionService eventTransactionService) {
        this.eventTransactionService = eventTransactionService;
}
    @PostMapping
    public Response<SimpleEventDto> createEvent(@Valid @RequestBody CreateEventTransDto dto){
        return ResponseFactory.ok(eventTransactionService.createCompleteEvent(dto));
    }
}
