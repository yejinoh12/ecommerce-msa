package com.orderservice.controller;

import com.common.utils.ParseRequestUtil;
import com.orderservice.dto.OrderReqDto;
import com.orderservice.service.OrderService;
import com.orderservice.service.ReturnAndCancelService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final ParseRequestUtil parseRequestUtil;
    private final ReturnAndCancelService returnAndCancelService;

    //주문 전 장바구니 조회
    @PostMapping
    public ResponseEntity<?> getCartItems(HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.getCartItems(userId));
    }

    //주문
    @PostMapping("/start")
    public ResponseEntity<?> order(@RequestBody OrderReqDto orderReqDto, HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.createOrder(orderReqDto, userId));
    }

    //주문 내역 보기
    @GetMapping("/list")
    public ResponseEntity<?> viewOrderList(HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.viewOrderList(userId));
    }

    //주문 내역 상세 보기
    @GetMapping("/details/{order_id}")
    public ResponseEntity<?> viewOrderDetails(@PathVariable("order_id") Long orderId, HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.viewOrderDetails(orderId, userId));
    }

    //취소하기
    @GetMapping("/cancel/{order_id}")
    public ResponseEntity<?> cancelOrder(@PathVariable("order_id") Long orderId) {
        return ResponseEntity.ok(returnAndCancelService.cancelOrder(orderId));
    }

    //반품하기
    @GetMapping("/return/{order_id}")
    public ResponseEntity<?> returnOrder(@PathVariable("order_id") Long orderId) {
        return ResponseEntity.ok(returnAndCancelService.returnOrder(orderId));
    }

}
