package com.orderservice.dto.order;

import com.common.dto.user.AddressResDto;
import lombok.Data;

import java.util.List;

@Data
public class OrderReqDto {
    List<OrderItemDto> items;
    AddressResDto address;
}