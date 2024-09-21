package com.orderservice.controller;

import com.common.utils.ParseRequestUtil;
import com.orderservice.dto.order.OrderPreviewDto;
import com.orderservice.dto.order.OrderReqDto;
import com.orderservice.service.CancelService;
import com.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CancelService cancelService;
    private final ParseRequestUtil parseRequestUtil;

    // 바로 구매 전 정보 조회
    @PostMapping("/direct/preview")
    public ResponseEntity<?> directOrderPreview(@RequestBody OrderReqDto orderReqDto, HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.directPurchasePreview(orderReqDto, userId));
    }

    // 바로 구매
    @PostMapping("/direct/process")
    public ResponseEntity<?> directOrder(@RequestBody OrderReqDto orderReqDto, HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.directOrder(orderReqDto, userId));
    }

    // 장바구니 상품 구매 전 정보 조회
    @GetMapping("/preview")
    public ResponseEntity<?> getCartItems(HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.getOrderItemsFromCart(userId));
    }

    // 장바구니 상품 구매
    @PostMapping("/process")
    public ResponseEntity<?> order(@RequestBody OrderPreviewDto orderPreviewDto, HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.orderFromCart(orderPreviewDto, userId));
    }

    //주문 내역 보기
    @GetMapping("/list")
    public ResponseEntity<?> viewOrderList(HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.viewOrderList(userId));
    }

    //주문 내역 상세 보기
    @GetMapping("/details/{orderId}")
    public ResponseEntity<?> viewOrderDetails(@PathVariable("orderId") Long orderId, HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.viewOrderDetails(orderId, userId));
    }

    //취소하기
    @GetMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(cancelService.cancelOrder(orderId));
    }

    //반품하기
    @GetMapping("/return/{orderId}")
    public ResponseEntity<?> returnOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(cancelService.returnOrder(orderId));
    }

    //바로 주문 테스트용 엔드포인트
    @PostMapping("/process/test")
    public ResponseEntity<?> orderTest(@RequestBody OrderPreviewDto orderPreviewDto) {
        Random random = new Random();
        Long userId = (long) (random.nextInt(1000) + 1);
        return ResponseEntity.ok(orderService.orderFromCart(orderPreviewDto, userId));
    }

}
