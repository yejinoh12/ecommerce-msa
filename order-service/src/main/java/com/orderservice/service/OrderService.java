package com.orderservice.service;

import com.common.dto.order.AvailCheckReqDto;
import com.common.dto.order.AvailCheckResDto;
import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentReqDto;
import com.common.dto.product.CartItemsDto;
import com.common.dto.user.AddressResDto;
import com.common.dto.user.UserInfoDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.client.UserServiceClient;
import com.orderservice.dto.order.*;
import com.orderservice.dto.orderHistory.OrderDetailsDto;
import com.orderservice.dto.orderHistory.OrderListDto;
import com.orderservice.entity.DeliveryStatus;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;
import com.orderservice.kafka.KafkaProducer;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public ApiResponse<OrderReqDto> directPurchasePreview(OrderReqDto orderReqDto, Long userId) {

        //이벤트 시간 및 재고 검증 DTO
        AvailCheckReqDto availCheckReqDto =
                new AvailCheckReqDto(orderReqDto.getProductId(), orderReqDto.getCnt());

        //확인 요청
        AvailCheckResDto availCheckResDto =
                productServiceClient.checkPurchaseAvailability(availCheckReqDto);

        //재고 검증
        if (!availCheckResDto.isHasStock()) {
            throw new BaseBizException("재고가 부족합니다.");
        }

        //판매 시간 검증
        if (!availCheckResDto.isInSalePeriod()) {
            throw new BaseBizException("판매 시간이 아닙니다.");
        }

        //배송지 정보
        AddressResDto addressResDto = userServiceClient.getDefaultAddress(userId);

        orderReqDto.setAddrAlias(addressResDto.getAlias());
        orderReqDto.setAddress(addressResDto.getAddress());
        orderReqDto.setAddrDetail(addressResDto.getDetailAddress());
        orderReqDto.setPhone(addressResDto.getPhone());

        return new ApiResponse<>(201, "결제를 진행해주세요.", orderReqDto);
    }

    //바로 구매
    @Transactional
    public ApiResponse<OrderResDto> directOrder(OrderReqDto orderReqDto, Long userId) {

        //레디스 재고 감소
        redisStockService.decreaseStockWithLock(orderReqDto.getProductId(), orderReqDto.getCnt());

        //최종 가격 계산
        int totalPrice = orderReqDto.getUnitPrice() * orderReqDto.getCnt();

        //주문 생성
        Order order = createAndSaveOrder(orderReqDto, userId, totalPrice);

        //주문 아이템 생성
        createOrderItems(order, orderReqDto);

        //재고 감소 상품 리스트
        List<UpdateStockReqDto> products = new ArrayList<>();
        products.add(new UpdateStockReqDto(orderReqDto.getProductId(), orderReqDto.getCnt()));

        // 결제 요청
        PaymentReqDto paymentRequest = new PaymentReqDto(order.getId(), totalPrice, products);
        kafkaProducer.sendPaymentRequest(paymentRequest);

        return new ApiResponse<>(201, "주문 요청 완료", OrderResDto.from(order));
    }


    //주문 전 장바구니 조회
    public ApiResponse<OrderPreviewDto> getOrderItemsFromCart(Long userId) {

        log.info("주문 전 장바구니 조회 : 사용자 ID = {}", userId);

        //장바구니에 있는 아이템 조회 요청
        List<CartItemsDto> cartItemsDtos = productServiceClient.getOrderItems(userId);

        //최종 가격 계산
        int totalOrderPrice = cartItemsDtos.stream()
                .mapToInt(cartItemsDto -> cartItemsDto.getUnitPrice() * cartItemsDto.getCnt())
                .sum();

        //배송지 정보 추가
        AddressResDto addressResDto = userServiceClient.getDefaultAddress(userId);

        //DTO 생성
        OrderPreviewDto responseDto = OrderPreviewDto.builder()
                .totalPrice(totalOrderPrice)
                .addressResDto(addressResDto)
                .orderReqItems(cartItemsDtos)
                .build();

        return new ApiResponse<>(201, "결제를 진행해주세요.", responseDto);
    }

    //주문
    @Transactional
    public ApiResponse<OrderResDto> orderFromCart(OrderPreviewDto orderReqDtos, Long userId) {

        log.info("주문 처리 시작: 사용자 ID = {}", userId);

        //레디스 재고 감소
        for (CartItemsDto dto : orderReqDtos.getOrderReqItems()) {
            redisStockService.decreaseStockWithLock(dto.getProductId(), dto.getCnt());
        }

        //최종 가격 계산
        int totalPrice = orderReqDtos.getTotalPrice();

        //주문 생성
        Order order = createAndSaveOrder(orderReqDtos.getAddressResDto(), userId, totalPrice);

        //주문 아이템 생성 및 재고 감소 상품 리스트 생성
        List<UpdateStockReqDto> products = new ArrayList<>();

        for (CartItemsDto cartItemsDto : orderReqDtos.getOrderReqItems()) {
            createOrderItems(order, cartItemsDto);
            products.add(new UpdateStockReqDto(cartItemsDto.getProductId(), cartItemsDto.getCnt()));
        }

        // 결제 요청
        PaymentReqDto paymentRequest = new PaymentReqDto(order.getId(), totalPrice, products);
        kafkaProducer.sendPaymentRequest(paymentRequest);

        return new ApiResponse<>(201, "주문 요청 완료", OrderResDto.from(order));
    }

    //주문 생성
    private Order createAndSaveOrder(OrderReqDto orderReqDto, Long userId, int totalPrice) {

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PAYMENT_IN_PROGRESS)
                .deliveryStatus(DeliveryStatus.PENDING)
                .addressAlias(orderReqDto.getAddrAlias())
                .address(orderReqDto.getAddress())
                .detailAddress(orderReqDto.getAddrDetail())
                .phone(orderReqDto.getPhone())
                .build();

        orderRepository.save(order);
        return order;
    }

    //장바구니 주문
    private Order createAndSaveOrder(AddressResDto addressResDto, Long userId, int totalPrice) {

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PAYMENT_IN_PROGRESS)
                .deliveryStatus(DeliveryStatus.PENDING)
                .addressAlias(addressResDto.getAlias())
                .address(addressResDto.getAddress())
                .detailAddress(addressResDto.getDetailAddress())
                .phone(addressResDto.getPhone())
                .build();

        orderRepository.save(order);
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

    //주문 아이템 생성
    private void createOrderItems(Order order, CartItemsDto cartItemsDto) {

        OrderItem orderItem = OrderItem.createOrderItem(
                order,
                cartItemsDto.getProductId(),
                cartItemsDto.getName(),
                cartItemsDto.getUnitPrice(),
                cartItemsDto.getCnt()
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

        //주문 정보
        OrderResDto orderResDto = OrderResDto.from(order);

        //사용자 정보
        UserInfoDto userInfoDto = userServiceClient.getUserInfo(userId);

        //배송지 정보
        AddressResDto addressResDto = AddressResDto.builder()
                .alias(order.getAddressAlias())
                .address(order.getAddress())
                .detailAddress(order.getDetailAddress())
                .phone(order.getPhone())
                .build();

        //주문 상품 정보
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderItemDto> orderItemDtos = orderItems.stream()
                .map(OrderItemDto::from)
                .collect(Collectors.toList());

        OrderDetailsDto orderDetailsDto = OrderDetailsDto.builder()
                .orderInfo(orderResDto)
                .addressInfo(addressResDto)
                .userInfo(userInfoDto)
                .items(orderItemDtos)
                .build();

        return ApiResponse.ok(200, "주문 상세 조회 성공", orderDetailsDto);
    }
}
