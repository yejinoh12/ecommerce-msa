package com.orderservice.dto;

import com.common.dto.user.UserInfoDto;
import lombok.*;

import java.util.List;

/**
 * 주문 정보 : 주문 번호, 주문 날짜, 주문 상태
 * 개인 정보 : 이름, 전화번호, 주소
 * 주문 제품 : 이름, 옵션, 수량
 */

@Data
@Builder
public class OrderDetailsDto {
    private OrderResDto oder_info;
    private UserInfoDto user_info;
    private List<OrderItemDto> order_items; // 장바구니 아이템 목록
}
