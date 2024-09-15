package com.orderservice.dto.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectOrderPreviewDto {
    private Long productId;
    private String name;
    private int quantity;
    private int totalPrice;
}
