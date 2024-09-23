package com.orderservice.controller;

import com.common.utils.ParseRequestUtil;
import com.orderservice.dto.order.OrderItemDto;
import com.orderservice.dto.order.OrderReqDto;
import com.orderservice.service.CancelService;
import com.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @PostMapping("/preview")
    public ResponseEntity<?> directOrderPreview(@RequestBody OrderItemDto orderItemDto, HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.ok(orderService.orderPreview(orderItemDto, userId));
    }

    // 주문 하기
    @PostMapping("/process")
    public ResponseEntity<?> directOrder(@RequestBody OrderReqDto orderReqDto, HttpServletRequest request) {
        Long userId = parseRequestUtil.extractUserIdFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.orderProcess(orderReqDto, userId));
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
    public ResponseEntity<?> orderTest(@RequestBody OrderReqDto orderReqDto) {
        Random random = new Random();
        Long userId = (long) (random.nextInt(1000) + 1);
        return ResponseEntity.ok(orderService.orderProcess(orderReqDto, userId));
    }

}
