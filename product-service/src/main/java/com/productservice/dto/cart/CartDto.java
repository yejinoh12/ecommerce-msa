package com.productservice.dto.cart;

import lombok.*;

import java.util.List;

@Data
@Builder
public class CartDto {
    private Long cartId;
    private List<CartItemDto> items;

}
