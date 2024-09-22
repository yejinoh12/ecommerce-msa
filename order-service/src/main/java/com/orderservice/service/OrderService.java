package com.orderservice.service;

import com.common.dto.order.AvailCheckReqDto;
import com.common.dto.order.AvailCheckResDto;
import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentReqDto;
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
    public ApiResponse<OrderReqDto> directOrderPreview(OrderReqDto orderReqDto, Long userId) {

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

    @Transactional
    public ApiResponse<OrderResDto> orderProcess(List<OrderReqDto> orderRequests, Long userId) {

        // 총 가격
        int totalPrice = 0;

        // 레디스 재고 감소 및 총 가격 계산
        for (OrderReqDto dto : orderRequests) {
            redisStockService.decreaseStockWithLock(dto.getProductId(), dto.getCnt());
            totalPrice += dto.getUnitPrice() * dto.getCnt();
        }

        // 주문 생성
        Order order = createAndSaveOrder(userId, totalPrice, orderRequests.getFirst());

        // 주문 아이템 생성 및 재고 감소 상품 리스트 생성
        List<UpdateStockReqDto> products = new ArrayList<>();

        for (OrderReqDto orderReqDto : orderRequests) {
            createOrderItems(order, orderReqDto);
            products.add(new UpdateStockReqDto(orderReqDto.getProductId(), orderReqDto.getCnt()));
        }

        // 결제 요청
        PaymentReqDto paymentRequest = new PaymentReqDto(order.getId(), totalPrice, products);
        kafkaProducer.sendPaymentRequest(paymentRequest);

        return new ApiResponse<>(201, "주문 요청 완료", OrderResDto.from(order));
    }

    // 주문 생성
    private Order createAndSaveOrder(Long userId, int totalPrice, OrderReqDto orderReqDto) {

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

    // 주문 아이템 생성
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
