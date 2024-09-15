package com.productservice.dto.cart;

import lombok.*;

@Data
@Builder
public class CartAddReqDto {
    private Long productId;
    private int cnt;
}

