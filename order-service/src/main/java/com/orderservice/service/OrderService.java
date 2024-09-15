package com.orderservice.service;

import com.common.dto.order.AvailCheckReqDto;
import com.common.dto.order.AvailCheckResDto;
import com.common.dto.payment.PaymentReqDto;
import com.common.dto.product.CartResDto;
import com.common.dto.user.UserInfoDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.client.UserServiceClient;
import com.orderservice.dto.order.*;
import com.orderservice.dto.orderHistory.OrderDetailsDto;
import com.orderservice.dto.orderHistory.OrderListDto;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.kafka.KafkaProducer;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final KafkaProducer kafkaProducer;
    private final RedisStockService redisStockService;

    //바로 구매 전 정보 확인
    public ApiResponse<DirectOrderPreviewDto> directPurchasePreview(OrderReqDto orderReqDto, Long userId) {

        //이벤트 시간 및 재고 검증 DTO
        AvailCheckReqDto availCheckReqDto =
                new AvailCheckReqDto(orderReqDto.getProductId(), orderReqDto.getCnt());

        //확인 요청
        AvailCheckResDto availCheckResDto = productServiceClient.checkPurchaseAvailability(availCheckReqDto);

        //재고 검증
        if (!availCheckResDto.isHasStock()) {
            throw new BaseBizException("재고가 부족합니다.");
        }

        //판매 시간 검증
        if (!availCheckResDto.isInSalePeriod()) {
            throw new BaseBizException("판매 시간이 아닙니다.");
        }

        //수령인 정보와 사용자 배송지 정보 추가

        //DTO 생성
        DirectOrderPreviewDto responseDto = DirectOrderPreviewDto.builder()
                .productId(orderReqDto.getProductId())
                .name(orderReqDto.getName())
                .quantity(orderReqDto.getCnt())
                .totalPrice(orderReqDto.getUnitPrice() * orderReqDto.getCnt())
                .build();

        return new ApiResponse<>(201, "결제를 진행해주세요.", responseDto);
    }

    //바로 구매
    @Transactional
    public ApiResponse<OrderResDto> directOrder(OrderReqDto orderReqDto, Long userId) {

        //레디스 재고 감소
        redisStockService.decreaseStockWithLock(orderReqDto.getProductId(), orderReqDto.getCnt());

        //최종 가격 계산
        int totalPrice = orderReqDto.getUnitPrice() * orderReqDto.getCnt();

        //주문 및 주문 아이템 생성
        Order order = createAndSaveOrder(userId, totalPrice);
        createOrderItems(order, orderReqDto);

        // 결제 요청
        PaymentReqDto paymentRequest = new PaymentReqDto(order.getId(), userId, totalPrice);
        kafkaProducer.sendPaymentRequest(paymentRequest);

        return new ApiResponse<>(201, "주문 요청 완료", OrderResDto.from(order));
    }


    //주문 전 장바구니 조회
    public ApiResponse<OrderPreviewDto> getOrderItemsFromCart(Long userId) {

        log.info("주문 전 장바구니 조회 : 사용자 ID = {}", userId);

        //장바구니에 있는 아이템 조회 요청
        List<CartResDto> cartResDtos = productServiceClient.getOrderItems(userId);

        //최종 가격 계산
        int totalOrderPrice = cartResDtos.stream()
                .mapToInt(cartResDto -> cartResDto.getUnitPrice() * cartResDto.getCnt())
                .sum();

        //수령인 정보와 사용자 배송지 정보 추가

        //DTO 생성
        OrderPreviewDto responseDto = OrderPreviewDto.builder()
                .totalPrice(totalOrderPrice)
                .orderReqItems(cartResDtos)
                .build();

        return new ApiResponse<>(201, "결제를 진행해주세요.", responseDto);
    }

    //주문
    @Transactional
    public ApiResponse<OrderResDto> orderFromCart(List<OrderReqDto> orderReqDtos, Long userId) {

        log.info("주문 처리 시작: 사용자 ID = {}", userId);

        //레디스 재고 감소
        for (OrderReqDto orderReqDto : orderReqDtos) {
            redisStockService.decreaseStockWithLock(orderReqDto.getProductId(), orderReqDto.getCnt());
        }

        //최종 가격 계산
        int totalPrice = orderReqDtos.stream()
                .mapToInt(orderReqDto -> orderReqDto.getUnitPrice() * orderReqDto.getCnt())
                .sum();

        //주문 생성
        Order order = createAndSaveOrder(userId, totalPrice);

        //주문 아이템 생성
        for (OrderReqDto orderReqDto : orderReqDtos) {
            createOrderItems(order, orderReqDto);
        }

        // 결제 요청
        PaymentReqDto paymentRequest = new PaymentReqDto(order.getId(), userId, totalPrice);
        kafkaProducer.sendPaymentRequest(paymentRequest);

        return new ApiResponse<>(201, "주문 요청 완료", OrderResDto.from(order));
    }

    //주문 생성
    private Order createAndSaveOrder(Long userId, int totalPrice) {
        Order order = Order.createOrder(userId, totalPrice);
        orderRepository.save(order);
        log.info("주문 생성 성공 orderId = {}, totalPrice={}", order.getId(), order.getTotalPrice());
        return order;
    }

    //주문 아이템 생성
    private void createOrderItems(Order order, OrderReqDto orderReqDto) {

        OrderItem orderItem = OrderItem.createOrderItem(
                order,
                orderReqDto.getProductId(),
                orderReqDto.getName(),
                orderReqDto.getUnitPrice(),
                orderReqDto.getCnt()
        );

        orderItemRepository.save(orderItem);
    }

    //주문 목록 조회
    public ApiResponse<List<OrderListDto>> viewOrderList(Long userId) {

        List<Order> orders = orderRepository.findByUserId(userId);
        List<OrderListDto> orderListDtos = orders.stream()
                .map(OrderListDto::from).toList();

        return ApiResponse.ok(200, "주문 목록 조회 성공", orderListDtos);
    }

    //주문 상세 내역
    public ApiResponse<OrderDetailsDto> viewOrderDetails(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseBizException("orderID " + orderId + "인 주문을 찾을 수 없습니다."));

        //1.주문 정보 DTO
        OrderResDto orderResDto = OrderResDto.from(order);

        //2.사용자 정보 DTO
        UserInfoDto userInfoDto = userServiceClient.getUserInfo(userId);

        //3.주문 했던 상품 정보 DTO
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderItemDto> orderItemDtos = orderItems.stream()
                .map(OrderItemDto::from)
                .collect(Collectors.toList());

        //응답 DTO
        OrderDetailsDto orderDetailsDto = OrderDetailsDto.builder()
                .orderInfo(orderResDto)
                .userInfo(userInfoDto)
                .items(orderItemDtos)
                .build();

        return ApiResponse.ok(200, "주문 상세 조회 성공", orderDetailsDto);
    }
}
