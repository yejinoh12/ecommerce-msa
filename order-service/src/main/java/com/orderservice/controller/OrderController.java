package com.orderservice.controller;

import com.common.dto.ApiResponse;
import com.common.utils.ParseRequestUtil;
import com.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    //장바구니에 있는 상품 주문
    @PostMapping
    public ResponseEntity<?> order(HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.createOrder(userId));
    }

    //주문 내역 보기
    @GetMapping("/list")
    public ResponseEntity<?> viewOrderList(HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.viewOrderList(userId));
    }

    //주문 내역 상세 보기
    @GetMapping("/details/{order_id}")
    public ResponseEntity<?> viewOrderDetails(@PathVariable("order_id") Long orderId,
                                                           HttpServletRequest request) {

        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        String token = new ParseRequestUtil().extractTokenFromRequest(request);
        log.info("token = {}", token);

        return ResponseEntity.ok(orderService.viewOrderDetails(orderId, userId, token));

    }
}
