package com.orderservice.service;

import com.common.dto.order.StockSyncReqDto;
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
import com.orderservice.exception.PaymentFailureException;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import com.orderservice.repository.RedisStockRepository;
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

    private final PaymentService paymentService;

    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RedisLockFacade redisLockFacade;
    private final RedisStockService redisStockService;
    private final RedisStockRepository redisStockRepository;

    /**********************************************************
     * 비즈니스 로직
     **********************************************************/

    //주문
    @Transactional
    public ApiResponse<OrderResDto> createOrder(Long userId) {

        log.info("주문 처리 시작: 사용자 ID = {}", userId);

        List<CreateOrderReqDto> dtos = productServiceClient.getOrderItems(userId);
        int totalPrice = calculateTotalPrice(dtos);
        Order order = createAndSaveOrder(userId, totalPrice);

        try {

            decreaseStockInRedis(dtos);                              //레디스 재고 감소
            paymentService.simulatePaymentProcessing(order.getId()); //고객 이탈 시뮬레이션(실패시 PaymentFailureException 발생)
            createOrderItems(order, dtos);                           //주문 아이템 생성

            requestStockSync(dtos, "DEC");                    //DB 재고 감소 요청(비동기)
            productServiceClient.deleteCartAfterOrder(userId);      //장바구니 삭제 요청

        } catch (PaymentFailureException e) {

            increaseStockInRedis(dtos);                             //실패 시 재고 롤백(DB 요청 전이기 때문에 DB 증가 요청을 보낼 필요 없음)
            log.error("결제 또는 재고 처리 실패: {}", e.getMessage());
            throw new BaseBizException(e.getMessage());

        }catch (Exception e){
            log.error("주문에 실패했습니다. : {}", e.getMessage());
        }

        log.info("주문 완료: 주문 ID = {}, 사용자 ID = {}", order.getId(), userId);
        return new ApiResponse<>(201, "주문이 완료되었습니다.", OrderResDto.from(order, userId));
    }

    /**********************************************************
     * 보조 메서드
     **********************************************************/

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

    //레디스 상품 재고 감소
    private void decreaseStockInRedis(List<CreateOrderReqDto> dtos) {
        List<UpdateStockReqDto> decreaseStockReqDtos = dtos.stream()
                .map(dto -> new UpdateStockReqDto(dto.getP_id(), dto.getCnt(), "DEC"))
                .collect(Collectors.toList());
        redisLockFacade.updateStockRedisson(decreaseStockReqDtos);
    }

    //레디스 상품 재고 증가 (주문 실패 시 재고 증가)
    private void increaseStockInRedis(List<CreateOrderReqDto> dtos) {
        List<UpdateStockReqDto> increaseStockReqDtos = dtos.stream()
                .map(dto -> new UpdateStockReqDto(dto.getP_id(), dto.getCnt(), "INC"))
                .collect(Collectors.toList());
        redisLockFacade.updateStockRedisson(increaseStockReqDtos);
    }

    /**********************************************************
     * 주문 관련 조회 메서드
     **********************************************************/

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
     * 상품 재고 DB 변경 요청 API
     **********************************************************/

    //데이터베이스와의 재고 동기화
    public void requestStockSync(List<CreateOrderReqDto> dtos, String action) {
        log.info("상품 DB 재고 감소 요청");
        List<UpdateStockReqDto> updateStockReqDtos = dtos.stream()
                .map(dto -> new UpdateStockReqDto(dto.getP_id(), dto.getCnt(), action))
                .collect(Collectors.toList());
        productServiceClient.requestStockSync(updateStockReqDtos);
    }
}
