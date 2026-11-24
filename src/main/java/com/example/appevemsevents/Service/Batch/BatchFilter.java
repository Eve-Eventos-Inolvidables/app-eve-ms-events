package com.example.appevemsevents.Service.Batch;

import com.example.appevecommon.Service.Utilities.Responses.PagedFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchFilter extends PagedFilter {
    private Long eventId;
}
