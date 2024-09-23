package com.orderservice.dto.orderHistory;

import com.common.dto.user.AddressResDto;
import com.common.dto.user.UserInfoDto;
import com.orderservice.dto.order.OrderItemDto;
import com.orderservice.dto.order.OrderResDto;
import lombok.*;

import java.util.List;

@Data
@Builder
public class OrderDetailsDto {
    private OrderResDto order;          //주문 정보
    private UserInfoDto user;           //사용자 정보
    private AddressResDto address;      //배송지 정보
    private List<OrderItemDto> items;   //주문아이템 정보
}
