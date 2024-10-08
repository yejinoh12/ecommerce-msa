package com.orderservice.service;

import com.common.dto.order.UpdateStockReqDto;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private static final int SHIPPED_DELAY = 24;
    private static final int DELIVERED_DELAY = 48;
    private static final int RETURN_DEADLINE = 24;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductServiceClient productServiceClient;
    private final RedisStockService redisStockService;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    @Transactional
    public void updateDeliveryStatuses() {
        LocalDateTime now = LocalDateTime.now();
        updateDeliveryStatus(now);
        updateReturnStatus(now);
    }

    //배송상태 변경
    private void updateDeliveryStatus(LocalDateTime now) {
        orderRepository.updateDeliveryStatusShipped(now.minusSeconds(SHIPPED_DELAY), now);
        orderRepository.updateDeliveryStatusDelivered(now.minusSeconds(DELIVERED_DELAY), now);
    }

    //반품상태 변경(반품진행 -> 반품완료) & 재고 복구
    private void updateReturnStatus(LocalDateTime now) {

        // 반품 진행 중인 상품 조회
        List<Order> returnedOrders = orderRepository.findByOrderStatus(OrderStatus.RETURN_REQ);

        // 반품 요청된 주문 ID를 담을 리스트
        List<Long> orderIds = new ArrayList<>();

        for (Order order : returnedOrders) {

            //반품일 + 1일 후에 재고 변경
            LocalDateTime returnDeadline = order.getModifiedAt().plusSeconds(RETURN_DEADLINE);

            if (now.isAfter(returnDeadline)) {
                orderIds.add(order.getId());
                order.updateStatusToReturned();
                orderRepository.save(order);
            }
        }

        if (!orderIds.isEmpty()) {

            //재고 복구 상품 리스트
            List<UpdateStockReqDto> updateStockReqDtos = orderItemRepository.findOrderItemDtosByOrderIds(orderIds);

            //레디스 및 DB 재고 복구
            for (UpdateStockReqDto dto : updateStockReqDtos) {
                redisStockService.increaseStock(dto.getProductId(), dto.getCnt());
                productServiceClient.increaseDBStock(dto);
                log.info("상품 {}의 재고 {}만큼 복구", dto.getProductId(), dto.getCnt());
            }
        }
    }
}
