package com.example.appevemsevents.Service.Precio;

import com.example.appevecommon.Service.Utilities.Responses.PagedFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrecioFilter extends PagedFilter {
    private Integer price;
}
