package com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResDto {

    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private int totalPrice;

}
