package com.productservice.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductStock {
    private Long p_id;
    private int stock;
}
