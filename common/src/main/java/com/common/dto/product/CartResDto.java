package com.common.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResDto {
    private Long productId;
    private int unitPrice;
    private int cnt;
}

