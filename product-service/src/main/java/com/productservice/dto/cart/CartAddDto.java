package com.productservice.dto.cart;

import lombok.*;

@Data
@Builder
public class CartAddDto {
    private Long productId;
    private int cnt;

}

