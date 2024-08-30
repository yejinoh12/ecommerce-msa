package com.productservice.dto.cart;

import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Long c_item_id;
    private Long p_id;
    private String p_name;
    private int price;
    private int cnt;

}

