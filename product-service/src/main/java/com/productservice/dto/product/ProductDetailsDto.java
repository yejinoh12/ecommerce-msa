package com.productservice.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
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
