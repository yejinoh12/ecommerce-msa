package com.orderservice.dto.order;

import lombok.Data;

@Data
public class OrderReqDto {
    private Long productId;
    private String name;
    private int unitPrice;
    private int cnt;
}
