package com.orderservice.service;

import com.common.dto.order.UpdateStockReqDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.common.dto.order.CreateOrderReqDto;
import com.common.dto.product.ProductInfoDto;
import com.common.dto.user.UserInfoDto;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.client.UserServiceClient;
import com.orderservice.dto.OrderDetailsDto;
import com.orderservice.dto.OrderItemDto;
import com.orderservice.dto.OrderResDto;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final PaymentService paymentService;

    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    //주문
    @Transactional
    public ApiResponse<OrderResDto> createOrder(Long userId) {

        log.info("주문 처리 시작: 사용자 ID = {}", userId);

        List<CreateOrderReqDto> dtos = productServiceClient.getOrderItems(userId);
        int totalPrice = calculateTotalPrice(dtos);
        Order order = createAndSaveOrder(userId, totalPrice);

        try {

            decreaseStockInRedis(dtos);                        //레디스 재고 감소

            ApiResponse<?> paymentResponse = paymentService.simulatePaymentProcessing(order.getId()); //고객 이탈 시뮬레이션
            if (paymentResponse.getStatus() != 200) {
                throw new BaseBizException("결제 실패: " + paymentResponse.getMessage());
            }

            createOrderItems(order, dtos);                     //주문 아이템 생성
            synchronizeStockWithDatabase(dtos);                //재고 동기화
            productServiceClient.deleteCartAfterOrder(userId); //장바구니 삭제 요청

        } catch (BaseBizException e) {
            rollbackStockInRedis(dtos);                       //실패 시 재고 롤백
            log.error("결제 또는 재고 처리 실패: {}", e.getMessage());
        }

        log.info("주문 완료: 주문 ID = {}, 사용자 ID = {}", order.getId(), userId);
        return new ApiResponse<>(201, "주문이 완료되었습니다.", OrderResDto.from(order, userId));
    }

    //주문 생성
    private Order createAndSaveOrder(Long userId, int totalPrice) {
        Order order = Order.createOrder(userId, totalPrice);
        orderRepository.save(order);
        log.info("주문 생성 성공 orderId = {}, totalPrice={}", order.getId(), order.getTotalPrice());
        return order;
    }

    //총 가격 계산
    private int calculateTotalPrice(List<CreateOrderReqDto> orderReqDtos) {
        return orderReqDtos.stream()
                .mapToInt(CreateOrderReqDto::getSubtotal)
                .sum();
    }

    //주문 아이템 생성
    private void createOrderItems(Order order, List<CreateOrderReqDto> orderReqDtos) {

        for (CreateOrderReqDto reqDto : orderReqDtos) {
            OrderItem orderItem = OrderItem.createOrderItem(
                    order,
                    reqDto.getP_id(),
                    reqDto.getSubtotal() / reqDto.getCnt(),
                    reqDto.getCnt()
            );
            orderItemRepository.save(orderItem); // 데이터베이스에 주문 아이템 저장
        }
    }

    //주문 목록 조회
    public ApiResponse<List<OrderResDto>> viewOrderList(Long userId) {

        List<Order> orders = orderRepository.findByUserId(userId);

        List<OrderResDto> orderResDtos = orders.stream()
                .map(order -> OrderResDto.from(order, userId))
                .collect(Collectors.toList());

        return ApiResponse.ok(200, "주문 목록 조회 성공", orderResDtos);
    }

    //주문 상세 내역
    public ApiResponse<OrderDetailsDto> viewOrderDetails(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseBizException("orderID " + orderId + "인 주문을 찾을 수 없습니다."));

        OrderResDto orderResDto = OrderResDto.from(order, userId);       //주문 정보 dto 생성

        UserInfoDto userInfoDto = userServiceClient.getUserInfo(userId); //유저 정보 dto 요청 및 생성
        log.info("user-service 요청 성공");

        List<OrderItemDto> orderItemDtos = getOrderItemDtos(orderItemRepository.findByOrderId(orderId)); //상품 아이템 dto 생성

        OrderDetailsDto orderDetailsDto = OrderDetailsDto.builder()
                .oder_info(orderResDto)
                .user_info(userInfoDto)
                .order_items(orderItemDtos)
                .build();

        return ApiResponse.ok(200, "주문 상세 조회 성공", orderDetailsDto);
    }

    //상품 아이템 조회
    private List<OrderItemDto> getOrderItemDtos(List<OrderItem> orderItems) {

        List<Long> productIds = orderItems.stream() //orderItems 에서 productOptionId 가져오기
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());

        //상품 요청 API 호출
        List<ProductInfoDto> productInfoDtos = productServiceClient.getProductInfos(productIds);

        Map<Long, ProductInfoDto> productInfoMap = productInfoDtos.stream()
                .collect(Collectors.toMap(ProductInfoDto::getProductId, dto -> dto)); //optionId -> key, dto -> value

        return orderItems.stream()
                .map(orderItem -> {
                    ProductInfoDto productInfoDto = productInfoMap.get(orderItem.getProductId());
                    if (productInfoDto == null) {
                        throw new BaseBizException("productOptionID " + orderItem.getProductId() + "에 대한 상품 정보를 찾을 수 없습니다.");
                    }
                    return OrderItemDto.from(productInfoDto, orderItem); //dto 반환
                })
                .collect(Collectors.toList());
    }

    /**********************************************************
     * 상품 재고 변경 관련 API 호출 메서드
     **********************************************************/

    //레디스 상품 재고 감소 요청
    private void decreaseStockInRedis(List<CreateOrderReqDto> dtos) {
        List<UpdateStockReqDto> decreaseStockReqDtos = dtos.stream()
                .map(dto -> new UpdateStockReqDto(dto.getP_id(), dto.getCnt(), "DEC"))
                .collect(Collectors.toList());
        productServiceClient.updateStock(decreaseStockReqDtos);
    }

    //레디스 상품 재고 롤백 요청(주문 실패 시 재고 증가)
    private void rollbackStockInRedis(List<CreateOrderReqDto> dtos) {
        List<UpdateStockReqDto> rollbackStockReqDtos = dtos.stream()
                .map(dto -> new UpdateStockReqDto(dto.getP_id(), dto.getCnt(), "INC"))
                .collect(Collectors.toList());
        productServiceClient.updateStock(rollbackStockReqDtos);
    }

    //데이터베이스와의 재고 동기화
    private void synchronizeStockWithDatabase(List<CreateOrderReqDto> dtos) {
        List<UpdateStockReqDto> synchronizeStockReqDtos = dtos.stream()
                .map(dto -> new UpdateStockReqDto(dto.getP_id(), dto.getCnt(), "SYNC"))
                .collect(Collectors.toList());
        productServiceClient.updateStock(synchronizeStockReqDtos);
    }
}
