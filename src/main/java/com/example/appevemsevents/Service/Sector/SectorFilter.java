package com.example.appevemsevents.Service.Sector;

import com.example.appevecommon.Service.Utilities.Responses.PagedFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectorFilter extends PagedFilter {
    private Long eventId;
}
