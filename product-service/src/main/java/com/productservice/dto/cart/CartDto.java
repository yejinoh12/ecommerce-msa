package com.productservice.dto.cart;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {
    private Long c_id;
    private List<CartItemDto> items;

}
