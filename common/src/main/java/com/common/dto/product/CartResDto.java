package com.common.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResDto {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private int price;
    private int cnt;
    private int subTotal;
}

