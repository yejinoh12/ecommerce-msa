package com.orderservice.dto;

import com.orderservice.entity.statusEnum.DeliveryStatus;
import com.orderservice.entity.statusEnum.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResDto {

    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private int totalPrice;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;

}
