package com.productservice.dto.cart;

import lombok.*;

@Data
@Builder
public class CartItemDto {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private int price;
    private int cnt;
}

