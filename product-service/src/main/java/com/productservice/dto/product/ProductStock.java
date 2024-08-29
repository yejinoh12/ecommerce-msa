package com.productservice.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductStock {
    private Long p_id;
    private int stock;
}
