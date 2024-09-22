package com.productservice.dto.cart;

import lombok.*;

@Data
@Builder
public class CartItemResDto {
    private Long cartItemId;
    private Long productId;
    private String name;
    private int unitPrice;
    private int quantity;
    private int subTotal;
    private boolean hasStock;
    private boolean isInSaleTime;
}

