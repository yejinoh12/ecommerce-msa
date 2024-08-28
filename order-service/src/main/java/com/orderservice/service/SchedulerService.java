package com.orderservice.service;

import com.common.dto.order.IncreaseStockReqDto;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductServiceClient productServiceClient;

    //@Scheduled(cron = "0 * * * * ?")  //1분마다
    //@Scheduled(cron = "0 0 0 * * ?")  //하루단위(매일 자정)

    //배송 상태 스케줄러
    //@Scheduled(cron = "0 * * * * ?")  //1분마다
    @Scheduled(cron = "0/24 * * * * ?") // 매 24초마다 실행
    @Transactional
    public void updateDeliveryStatuses() {

        LocalDateTime now = LocalDateTime.now();

        updateDeliveryStatusesShipped(now);
        updateDeliveryStatusesDelivered(now);

        updateReturnStatuses(now);

    }

    //배송준비 -> 배송중
    private void updateDeliveryStatusesShipped(LocalDateTime now) {
        orderRepository.updateDeliveryStatusShipped(now, now);
    }

    //배송중 -> 배송완료
    private void updateDeliveryStatusesDelivered(LocalDateTime now) {
        orderRepository.updateDeliveryStatusDelivered(now.minusSeconds(24), now);
    }

    //반품진행 -> 반품완료 //재고 복구
    private void updateReturnStatuses(LocalDateTime now) {

        List<Order> returnedOrders = orderRepository.findByOrderStatus(OrderStatus.RETURN_ING); // 반품 진행 중인 상품을 가져와서
        List<Long> orderIds = new ArrayList<>();
        List<IncreaseStockReqDto> increaseStockReqDtos = new ArrayList<>(); // 반품 신청돼서 재고 증가시킬 상품들

        for (Order order : returnedOrders) {

            LocalDateTime returnDeadline = order.getModifiedAt().plusSeconds(24); //반품일 + 1일이 데드라인(테스트 24초)

            if (now.isAfter(returnDeadline)) { // 반품 신청 후 1일이 지나면 재고 증가 로직 실행

                orderIds.add(order.getId());
                order.updateStatusToReturned(); //상태 변경
                orderRepository.save(order);
            }
        }


        // 주문 아이템의 재고 증가 요청을 한 번에 조회
        if (!orderIds.isEmpty()) {

            increaseStockReqDtos = orderItemRepository.findOrderItemDtosByOrderIds(orderIds);
            log.info("요청 상품 아이템 개수 = {}", increaseStockReqDtos.size());

            // 재고 증가 요청이 있을 경우 ProductServiceClient를 통해 재고 복구
            if (!increaseStockReqDtos.isEmpty()) {
                productServiceClient.increaseStock(increaseStockReqDtos);
            }

        }else{
            log.info("반품할 상품이 없음");
        }

        orderRepository.updateOrderStatusAfterReturn(now.minusSeconds(24), now); // 반품 상태 업데이트

    }
}
