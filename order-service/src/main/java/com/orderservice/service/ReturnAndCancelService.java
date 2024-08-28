package com.orderservice.service;


import com.common.dto.ApiResponse;
import com.common.dto.order.IncreaseStockReqDto;
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
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReturnAndCancelService {

    private final ProductServiceClient productServiceClient;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    //취소하기
    public ApiResponse<?> cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다."));

        //현재 상태가 배송준비(PENDING)일 때만 가능
        if (order.getDeliveryStatus() != (DeliveryStatus.PENDING)) {
            throw new RuntimeException("취소가 불가능 합니다.");
        } else {

            List<IncreaseStockReqDto> increaseStockReqDtos = orderItemRepository.findOrderItemDtosByOrderId(orderId);
            productServiceClient.increaseStock(increaseStockReqDtos);
            order.updateStatusToCanceled(); // 주문 상태만 반품 진행중으로 변경

        }
        return ApiResponse.ok(200, "주문 취소 성공", null);
    }

    //반품하기
    public ApiResponse<String> returnOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime returnDeadline = order.getModifiedAt().plusSeconds(24); // 배송 완료 후 D+1(24초)까지 반품 가능

        //배송완료 된 제품만 가능, 배송 완료 후 D+1 까지 반품 가능
        if (order.getDeliveryStatus() == DeliveryStatus.DELIVERED && now.isBefore(returnDeadline)) {

            order.updateStatusToReturning(); // 재고 복구 & 주문 상태 변경

        }else{
            throw new RuntimeException("반품 기한이 지났습니다.");
        }

        return ApiResponse.ok(200,"반품 신청 성공", null);
    }

}
