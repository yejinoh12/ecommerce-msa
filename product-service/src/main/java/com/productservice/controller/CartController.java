package com.productservice.controller;

import com.common.utils.ParseRequestUtil;
import com.productservice.dto.cart.CartAddReqDto;
import com.productservice.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    //장바구니 담기
    @PostMapping("/add")
    public ResponseEntity<?> addCartItem(@RequestBody CartAddReqDto cartAddReqDto, HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addCartItem(cartAddReqDto, userId));
    }

    //장바구니 상품 수량 증가
    @GetMapping("/increase/{cartItemId}")
    public ResponseEntity<?> incrementCartItem(@PathVariable("cartItemId") Long cartItemId, HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.incrementCartItem(cartItemId, userId));
    }

    //장바구니 상품 수량 감소
    @GetMapping("/decrease/{cartItemId}")
    public ResponseEntity<?> decrementCartItem(@PathVariable("cartItemId") Long cartItemId, HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.decreaseCartItem(cartItemId, userId));
    }

    //장바구니 비우기
    @GetMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    //장바구니 조회
    @GetMapping
    public ResponseEntity<?> viewCartItems(HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.viewCartItems(userId));
    }

    //장바구니 주문
    @GetMapping("/order")
    public ResponseEntity<?> orderCartItems(HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.orderCartItems(userId));
    }

    //주문 서비스에서 장바구니 조회
    @GetMapping("/get-items")
    public ResponseEntity<?> getCartItems(@RequestHeader("X-Claim-userId") Long userId) {
        return ResponseEntity.ok(cartService.getCartItemsForOrder(userId));
    }

    //주문서비스에서 장바구니 삭제
    @GetMapping("/clear/after-order")
    public ResponseEntity<?> clearCartAfterOrder(@RequestHeader("X-Claim-userId") Long userId) {
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

}
