package com.productservice.dto.product;


import com.common.dto.product.ProductInfoDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.productservice.domain.product.Product;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailsDto {
    private Long p_id;
    private String p_name;
    private int price;
    private String type;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
}
