package com.orderservice.service;

import com.common.dto.payment.PaymentReqDto;
import com.common.dto.product.CartResDto;
import com.common.dto.product.ProductInfoDto;
import com.common.dto.user.UserInfoDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.client.UserServiceClient;
import com.orderservice.dto.*;
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
import java.util.Map;
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
    private final StockCacheService stockCacheService;

    //바로 구매
    public ApiResponse<OrderResDto> directPurchase(OrderReqDto orderReqDto, Long userId) {
        return new ApiResponse<>(201, "결제를 진행해주세요.", null);
    }

    //주문 전 장바구니 조회
    //수령인 정보와 배송지 정보를 추가해야함
    public ApiResponse<OrderPreviewResDto> getOrderItemsFromCart(Long userId) {

        log.info("주문 전 장바구니 조회 : 사용자 ID = {}", userId);

        //장바구니에 있는 아이템 조회 요청
        List<CartResDto> cartResDtos = productServiceClient.getOrderItems(userId);

        //최종 가격 계산
        int totalOrderPrice = cartResDtos.stream()
                .mapToInt(cartResDto -> cartResDto.getUnitPrice() * cartResDto.getCnt())
                .sum();

        //DTO 생성
        OrderPreviewResDto responseDto = OrderPreviewResDto.builder()
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
        for(OrderReqDto orderReqDto : orderReqDtos){
            stockCacheService.decreaseStock(orderReqDto.getProductId(), orderReqDto.getCnt());
        }

        //최종 가격 계산
        int totalPrice = orderReqDtos.stream()
                .mapToInt(orderReqDto -> orderReqDto.getUnitPrice() * orderReqDto.getCnt())
                .sum();

        //주문 및 주문 아이템 생성
        Order order = createAndSaveOrder(userId, totalPrice);
        createOrderItems(order, orderReqDtos);

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
    private void createOrderItems(Order order, List<OrderReqDto> orderReqDtos) {

        for (OrderReqDto orderReqDto : orderReqDtos) {
            OrderItem orderItem = OrderItem.createOrderItem(
                    order,
                    orderReqDto.getProductId(),
                    orderReqDto.getUnitPrice(),
                    orderReqDto.getCnt()
            );
            orderItemRepository.save(orderItem);
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

        List<ProductInfoDto> productInfoDtos = productServiceClient.getProductInfos(productIds);

        Map<Long, ProductInfoDto> productInfoMap = productInfoDtos.stream()
                .collect(Collectors.toMap(ProductInfoDto::getProductId, dto -> dto)); //optionId -> key, dto -> value

        return orderItems.stream()
                .map(orderItem -> {
                    ProductInfoDto productInfoDto = productInfoMap.get(orderItem.getProductId());
                    if (productInfoDto == null) {
                        throw new BaseBizException("productID " + orderItem.getProductId() + "를 찾을 수 없습니다.");
                    }
                    return OrderItemDto.from(productInfoDto, orderItem); //dto 반환
                })
                .collect(Collectors.toList());
    }
}
