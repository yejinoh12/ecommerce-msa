package com.orderservice.service;

import com.common.dto.ApiResponse;
import com.common.dto.order.CreateOrderReqDto;
import com.common.dto.order.DecreaseStockReqDto;
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
import com.orderservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;


    @Transactional
    public ApiResponse<OrderResDto> createOrder(Long userId) {

        log.info("주문 로직 시작");

        Map<Order, List<CreateOrderReqDto>> orderReqDtos = createAndSaveOrder(userId);

        Order order = orderReqDtos.keySet().iterator().next();
        List<CreateOrderReqDto> dtos = orderReqDtos.get(order);

        log.info("첫번째 주문 아이템 옵션 확인 = {}", dtos.get(0).getProductOptionId());
        log.info("주문 아이템 생성 및 재고 감소 로직 시작");

        createOrderItemAndUpdateStock(order, dtos);

        OrderResDto orderResDto = OrderResDto.builder()
                .orderId(order.getId())
                .userId(userId)
                .orderDate(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .deliveryStatus(order.getDeliveryStatus())
                .build();

        return new ApiResponse<>(201, "주문이 완료되었습니다.", orderResDto);
    }

    /**
     * 주문 생성
     * @return List<CreateOrderReqDto> : 제품서비스에서 사용자 장바구니에 있는 아이템을 가져와서 반환
     */

    private Map<Order, List<CreateOrderReqDto>> createAndSaveOrder(Long userId) {

        //장바구니 불러오는 API 호출
        List<CreateOrderReqDto> orderReqDtos = productServiceClient.getOrderItems(userId);

        int totalPrice = orderReqDtos.stream()
                .mapToInt(CreateOrderReqDto::getSubtotal)
                .sum();

        Order order = Order.createOrder(userId, totalPrice);
        orderRepository.save(order);

        return Map.of(order, orderReqDtos);
    }


    /**
     * 주문 아이템 생성 및 재고 감소
     */

    private void createOrderItemAndUpdateStock(Order order, List<CreateOrderReqDto> orderReqDtos) {

        List<DecreaseStockReqDto> decreaseStockReqDtos = new ArrayList<>();

        for (CreateOrderReqDto reqDto : orderReqDtos) {

            OrderItem orderItem = OrderItem.createOrderItem(
                    order,
                    reqDto.getProductOptionId(),
                    reqDto.getSubtotal() / reqDto.getQuantity(), // unitPrice 계산
                    reqDto.getQuantity()
            );

            orderItemRepository.save(orderItem);

            decreaseStockReqDtos.add(new DecreaseStockReqDto(reqDto.getProductOptionId(), reqDto.getQuantity()));
        }

        log.info("재고 감소 아이템 중 첫번째 확인 = {}", decreaseStockReqDtos.get(0).getProductOptionId());

        //재고 감소 API 호출
        productServiceClient.decreaseStock(decreaseStockReqDtos);
    }

    //주문 목록 조회
    public ApiResponse<List<OrderResDto>> viewOrderList(Long userId){

        List<Order> orders = orderRepository.findByUserId(userId);

        List<OrderResDto> orderResDtos = new ArrayList<>();

        for(Order order : orders){
            OrderResDto orderResDto = OrderResDto.builder()
                    .orderId(order.getId())
                    .userId(userId)
                    .orderDate(order.getCreatedAt())
                    .totalPrice(order.getTotalPrice())
                    .orderStatus(order.getOrderStatus())
                    .deliveryStatus(order.getDeliveryStatus())
                    .build();

            orderResDtos.add(orderResDto);
        }

        return ApiResponse.ok(200,"주문 목록 조회 성공", orderResDtos);
    }

    //주문 상세 내역 조회
    public ApiResponse<OrderDetailsDto> viewOrderDetails(Long orderId, Long userId, String token) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));


        OrderResDto orderResDto = OrderResDto.builder()
                .orderId(order.getId())
                .userId(userId)
                .orderDate(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .deliveryStatus(order.getDeliveryStatus())
                .build();

        log.info("user-service 요청 시작");
        UserInfoDto userInfoDto = userServiceClient.getUserInfo(token);
        log.info("요청 완료 userId = {}", userInfoDto.getName());

        List<OrderItemDto> orderItemDtos = getOrderItemDtos(orderItemRepository.findByOrderId(orderId));

        OrderDetailsDto orderDetailsDto = OrderDetailsDto.builder()
                .oder_info(orderResDto)
                .user_info(userInfoDto)
                .order_items(orderItemDtos)
                .build();

        return ApiResponse.ok(200,"주문 상세 조회 성공", orderDetailsDto);
    }


    //상품 아이템 조회
    private List<OrderItemDto> getOrderItemDtos(List<OrderItem> orderItems) {

        List<Long> productOptionItemIds = orderItems.stream()
                .map(OrderItem::getProductOptionId)
                .collect(Collectors.toList());

        //상품 요청 API 호출
        List<ProductInfoDto> productInfoDtos = productServiceClient.getProductInfos(productOptionItemIds);

        Map<Long, ProductInfoDto> productInfoMap = productInfoDtos.stream()
                .collect(Collectors.toMap(ProductInfoDto::getProductOptionId, dto -> dto));

        return orderItems.stream()
                .map(orderItem -> {
                    ProductInfoDto productInfoDto = productInfoMap.get(orderItem.getProductOptionId());
                    if (productInfoDto == null) {
                        throw new NoSuchElementException("Product information not found for option ID: " + orderItem.getProductOptionId());
                    }
                    return new OrderItemDto(
                            productInfoDto.getName(),
                            productInfoDto.getOpt(),
                            orderItem.getQuantity(),
                            orderItem.getUnitPrice() * orderItem.getQuantity()
                    );
                })
                .collect(Collectors.toList());
    }
}
