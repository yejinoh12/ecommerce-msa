package com.orderservice.service;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.CartResDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.common.dto.order.CreateOrderReqDto;
import com.common.dto.product.ProductInfoDto;
import com.common.dto.user.UserInfoDto;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.client.UserServiceClient;
import com.orderservice.dto.*;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.exception.PaymentFailureException;
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
public class
OrderService {

    private final PaymentService paymentService;

    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    //주문 전 장바구니 조회
    public ApiResponse<CartInfoResDto> getCartItems(Long userId) {

        log.info("주문 전 장바구니 조회 : 사용자 ID = {}", userId);

        List<CartResDto> cartResDtos = productServiceClient.getOrderItems(userId);

        int totalOrderPrice = cartResDtos.stream()
                .mapToInt(CartResDto::getSubTotal)
                .sum();

        CartInfoResDto responseDto = CartInfoResDto.builder()
                .userId(userId)
                .totalPrice(totalOrderPrice)
                .cartResDtos(cartResDtos)
                .build();

        return new ApiResponse<>(201, "결제를 진행해주세요.", responseDto);
    }

    //주문
    @Transactional
    public ApiResponse<OrderResDto> createOrder(OrderReqDto orderReqDto, Long userId) {

        log.info("주문 처리 시작: 사용자 ID = {}", userId);
        Order order = createAndSaveOrder(userId, orderReqDto.getTotalPrice());       //주문생성

        try {

            requestUpdateStock(orderReqDto, "RD");                            //레디스 감소 요청
            paymentService.simulatePaymentProcessing(order.getId());                //고객 이탈

            createOrderItems(order, orderReqDto);                                   //주문 아이템 생성
            requestUpdateStock(orderReqDto, "DEC");                           //DB 재고 맞추기
            productServiceClient.deleteCartAfterOrder(userId);                      //장바구니 삭제 요청

        } catch (PaymentFailureException e) {

            requestUpdateStock(orderReqDto, "RI");                          //레디스 재고 증가
            log.error("결제 처리 실패: {}", e.getMessage());
            throw new BaseBizException(e.getMessage());

        }catch (Exception e) {
            log.error("주문 처리 중 오류 발생: {}", e.getMessage());
            throw e; // 예외를 다시 던져서 트랜잭션 롤백
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

    //주문 아이템 생성
    private void createOrderItems(Order order, OrderReqDto orderReqDtos) {

        for (CartResDto cartResDto : orderReqDtos.getCartResDtos()) {
            OrderItem orderItem = OrderItem.createOrderItem(
                    order,
                    cartResDto.getProductId(),
                    cartResDto.getPrice(),
                    cartResDto.getCnt()
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
                        throw new BaseBizException("productOptionID " + orderItem.getProductId() + "에 대한 상품 정보를 찾을 수 없습니다.");
                    }
                    return OrderItemDto.from(productInfoDto, orderItem); //dto 반환
                })
                .collect(Collectors.toList());
    }

    //상품 서비스 요청 API
    public void requestUpdateStock(OrderReqDto orderReqDto, String action) {
        log.info("재고 변경 요청, action={}", action);
        List<UpdateStockReqDto> updateStockReqDtos = orderReqDto.getCartResDtos().stream()
                .map(cartResDto -> new UpdateStockReqDto(cartResDto.getProductId(), cartResDto.getCnt()))
                .collect(Collectors.toList());

        productServiceClient.updateStock(updateStockReqDtos, action);
    }
}
