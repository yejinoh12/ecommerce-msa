package com.productservice.dto.product;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailsDto {

    private Long p_id;
    private String p_name;
    private int price;
    private String type;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;

}
