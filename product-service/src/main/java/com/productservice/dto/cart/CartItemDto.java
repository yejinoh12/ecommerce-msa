package com.productservice.dto.cart;

import lombok.*;

@Data
@Builder
public class CartItemDto {
    private Long c_item_id;
    private String name;
    private int price;
    private int cnt;
    private boolean hasStock;
    private boolean isInSaleTime;
}

