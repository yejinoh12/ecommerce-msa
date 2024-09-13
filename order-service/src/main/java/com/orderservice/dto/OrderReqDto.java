package com.orderservice.dto;

import lombok.Data;

@Data
public class OrderReqDto {
    private Long productId;
    private int unitPrice;
    private int cnt;
}
