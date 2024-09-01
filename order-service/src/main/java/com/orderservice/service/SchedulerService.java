package com.orderservice.service;

import com.common.dto.order.UpdateStockReqDto;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.entity.Order;
import com.orderservice.entity.statusEnum.OrderStatus;
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
import java.util.concurrent.CompletableFuture;

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
    private final RedisLockFacade redisLockFacade;

    @Scheduled(cron = "0/24 * * * * ?") // 매 24초마다 실행
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

        List<Order> returnedOrders = orderRepository.findByOrderStatus(OrderStatus.RETURN_ING); // 반품 진행 중인 상품을 가져옴
        List<Long> orderIds = new ArrayList<>();                                                // 반품 요청된 주문 ID를 담을 리스트

        for (Order order : returnedOrders) {
            LocalDateTime returnDeadline = order.getModifiedAt().plusSeconds(RETURN_DEADLINE); //반품일 + 1일 후에 재고 변경
            if (now.isAfter(returnDeadline)) {
                orderIds.add(order.getId());
                order.updateStatusToReturned(); //상태를 환불완료로 변경
                orderRepository.save(order);
            }
        }

        if (!orderIds.isEmpty()) {
            List<UpdateStockReqDto> updateStockReqDtos = orderItemRepository.findOrderItemDtosByOrderIds(orderIds);


            //재고 변경(redis, db)
            redisLockFacade.updateStockRedisson(updateStockReqDtos);
            CompletableFuture.runAsync(() -> productServiceClient.requestStockSync(updateStockReqDtos))
                    .exceptionally(ex -> {
                        log.error("Error occurred while syncing stock", ex);
                        return null;
                    });

            //productServiceClient.updateStock(updateStockReqDtos); //상품 서비스에 재고 증가 요청
        }


    }
}
