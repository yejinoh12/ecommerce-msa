package com.orderservice.service;

import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
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

    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 주문
     */
    @Transactional
    public ApiResponse<OrderResDto> createOrder(Long userId) {

        log.info("주문 처리 시작: 사용자 ID = {}", userId);

        Map<Order, List<CreateOrderReqDto>> orderReqDtos = createAndSaveOrder(userId);

        Order order = orderReqDtos.keySet().stream().findFirst()
                .orElseThrow(() -> new BaseBizException("주문이 생성되지 않았습니다.")); //map's key

        List<CreateOrderReqDto> dtos = orderReqDtos.get(order);      //map's value(주문 요청 상품 list)
        createOrderItemAndUpdateStock(order, dtos);                  //주문 아이템 생성 및 재고 감소
        OrderResDto orderResDto = OrderResDto.from(order, userId);   //Dto 생성

        log.info("주문 완료: 주문 ID = {}, 사용자 ID = {}", order.getId(), userId);

        return new ApiResponse<>(201, "주문이 완료되었습니다.", orderResDto);
    }

    /**
     * 주문 객체 생성
     * @return List<CreateOrderReqDto> : 제품서비스에서 사용자 장바구니에 있는 아이템을 가져와서 반환
     */
    private Map<Order, List<CreateOrderReqDto>> createAndSaveOrder(Long userId) {

        List<CreateOrderReqDto> orderReqDtos = productServiceClient.getOrderItems(userId);
        log.info("상품 서비스에서 사용자 장바구니 조회 요청 성공");

        int totalPrice = orderReqDtos.stream()
                .mapToInt(CreateOrderReqDto::getSubtotal)
                .sum();

        Order order = Order.createOrder(userId, totalPrice);
        orderRepository.save(order);

        return Map.of(order, orderReqDtos);
    }


    /**
     * 주문 아이템 객체 생성 및 재고 감소
     */
    private void createOrderItemAndUpdateStock(Order order, List<CreateOrderReqDto> orderReqDtos) {

        List<DecreaseStockReqDto> decreaseStockReqDtos = new ArrayList<>();

        for (CreateOrderReqDto reqDto : orderReqDtos) {

            OrderItem orderItem = OrderItem.createOrderItem(
                    order,
                    reqDto.getProductOptionId(),
                    reqDto.getSubtotal() / reqDto.getQuantity(), // unitPrice
                    reqDto.getQuantity()
            );

            orderItemRepository.save(orderItem);
            decreaseStockReqDtos.add(new DecreaseStockReqDto(reqDto.getProductOptionId(), reqDto.getQuantity()));
        }

        productServiceClient.decreaseStock(decreaseStockReqDtos);
        log.info("상품 서비스에서 재고 감소 요청 성공");
    }

    /**
     * 주문 목록 조회
     */
    public ApiResponse<List<OrderResDto>> viewOrderList(Long userId) {

        List<Order> orders = orderRepository.findByUserId(userId);

        List<OrderResDto> orderResDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderResDto orderResDto = OrderResDto.from(order, userId);
            orderResDtos.add(orderResDto);
        }

        return ApiResponse.ok(200, "주문 목록 조회 성공", orderResDtos);
    }

    /**
     * 주문 상세 내역 조회
     */
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

    /**
     * 상품 아이템 조회
     */
    private List<OrderItemDto> getOrderItemDtos(List<OrderItem> orderItems) {

        List<Long> productOptionItemIds = orderItems.stream() //orderItems 에서 productOptionId 가져오기
                .map(OrderItem::getProductOptionId)
                .collect(Collectors.toList());

        //상품 요청 API 호출
        List<ProductInfoDto> productInfoDtos = productServiceClient.getProductInfos(productOptionItemIds);

        Map<Long, ProductInfoDto> productInfoMap = productInfoDtos.stream()
                .collect(Collectors.toMap(ProductInfoDto::getProductOptionId, dto -> dto)); //optionId -> key, dto -> value

        return orderItems.stream()
                .map(orderItem -> {
                    ProductInfoDto productInfoDto = productInfoMap.get(orderItem.getProductOptionId());
                    if (productInfoDto == null) {
                        throw new BaseBizException("productOptionID " + orderItem.getProductOptionId() + "에 대한 상품 정보를 찾을 수 없습니다.");
                    }
                    return OrderItemDto.from(productInfoDto, orderItem); //dto 반환
                })

                .collect(Collectors.toList());
    }
}
