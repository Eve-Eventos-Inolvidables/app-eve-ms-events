package com.example.appevemsevents.Service.EventSimple;

import java.time.LocalDate;

import com.example.appevecommon.Service.Utilities.Responses.PagedFilter;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class EventSimpleFilter extends PagedFilter {
    private String name;
    private Long categoryId;
    private LocalDate visibleStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    /**
     * Exclusive upper bound for the event date.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateBefore;
}
