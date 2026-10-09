package com.example.appevemsevents.Service.EventSimple;

import java.time.LocalDate;

import com.example.appevecommon.Service.Utilities.Responses.PagedFilter;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class EventSimpleFilter extends PagedFilter {
    private String name;
    private Long categoryId;
    private LocalDate visibleStart;
}
