package com.productservice.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProductStockResDto {
    private Long p_id;
    private int stock;
}
