package com.productservice.dto.cart;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CartDto {

    private Long c_id;
    private List<CartItemDto> items;

}
