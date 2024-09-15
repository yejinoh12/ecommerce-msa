package com.productservice.dto.cart;

import lombok.*;

@Data
@Builder
public class CartItemResDto {
    private Long c_item_id;
    private String name;
    private int unitPrice;
    private int quantity;
    private int subTotal;
    private boolean hasStock;
    private boolean isInSaleTime;
}

