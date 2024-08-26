package com.orderservice.dto;

import com.orderservice.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private String name;
    private String opt;
    private int cnt;
    private int price;

}
