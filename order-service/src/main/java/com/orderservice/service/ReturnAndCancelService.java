package com.orderservice.service;


import com.common.dto.order.UpdateStockReqDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.entity.Order;
import com.orderservice.entity.statusEnum.DeliveryStatus;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReturnAndCancelService {

    private final ProductServiceClient productServiceClient;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    //주문 취소 : 현재 상태가 배송준비(PENDING)일 때만 취소 가능
    public ApiResponse<?> cancelOrder(Long orderId) {

        Order order = findOrderById(orderId);
        if (order.getDeliveryStatus() != (DeliveryStatus.PENDING)) {
            throw new BaseBizException("취소가 불가능 합니다.");
        }

        List<UpdateStockReqDto> updateStockReqDtos = orderItemRepository.findOrderItemDtosByOrderId(orderId);
        productServiceClient.updateRedisStock(updateStockReqDtos, "INC");

        order.updateStatusToCanceled();

        return ApiResponse.ok(200, "주문 취소 성공", null);
    }

    //반품하기 : 배송완료 된 제품만 가능, 배송 완료 후 D+1 까지 반품 가능)
    public ApiResponse<String> returnOrder(Long orderId) {

        Order order = findOrderById(orderId);

        //반품 가능 상태 확인
        if (order.getDeliveryStatus() != DeliveryStatus.DELIVERED) {
            throw new BaseBizException("반품 가능 시간이 아닙니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime returnDeadline = order.getModifiedAt().plusDays(1);

        if (now.isAfter(returnDeadline)) {
            throw new BaseBizException("반품 기한이 지났습니다.");
        }

        order.updateStatusToReturning(); // 상태 변경

        return ApiResponse.ok(200, "반품 신청 성공", null);
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseBizException("orderID " + orderId + "인 주문을 찾을 수 없습니다."));
    }
}
