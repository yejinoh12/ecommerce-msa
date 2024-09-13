package com.productservice.dto.cart;

import lombok.*;

import java.util.List;

@Data
@Builder
public class CartDto {
    private int totalPrice;
    private List<CartItemDto> items;
}
