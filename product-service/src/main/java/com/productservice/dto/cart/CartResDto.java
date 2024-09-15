package com.productservice.dto.cart;

import lombok.*;

import java.util.List;

@Data
@Builder
public class CartResDto {
    private int totalPrice;
    private List<CartItemResDto> items;
}
