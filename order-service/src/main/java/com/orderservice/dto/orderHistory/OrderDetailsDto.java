package com.orderservice.dto.orderHistory;

import com.common.dto.user.AddressResDto;
import com.common.dto.user.UserInfoDto;
import com.orderservice.dto.order.OrderItemDto;
import com.orderservice.dto.order.OrderResDto;
import lombok.*;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;

import java.util.List;

/**
 * 주문 정보 : 주문 번호, 주문 날짜, 주문 상태
 * 개인 정보 : 이름, 전화번호, 주소
 * 주문 제품 : 이름, 옵션, 수량
 */

@Data
@Builder
public class OrderDetailsDto {
    private OrderResDto orderInfo;
    private UserInfoDto userInfo;
    private AddressResDto addressInfo;
    private List<OrderItemDto> items;
}
