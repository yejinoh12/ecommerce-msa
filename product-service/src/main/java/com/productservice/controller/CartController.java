package com.productservice.controller;

import com.common.dto.order.CreateOrderReqDto;
import com.common.utils.ParseRequestUtil;
import com.productservice.dto.cart.CartAddDto;
import com.productservice.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    //장바구니 담기
    @PostMapping("/add")
    public ResponseEntity<?> addCartItem(@RequestBody CartAddDto cartAddDto, HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addCartItem(cartAddDto, userId));
    }

    //장바구니 상품 수량 증가
    @GetMapping("/increase/{cart_item_id}")
    public ResponseEntity<?> incrementCartItem(@PathVariable("cart_item_id") Long cartItemId, HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.incrementCartItem(cartItemId, userId));
    }

    //장바구니 상품 수량 감소
    @GetMapping("/decrease/{cart_item_id}")
    public ResponseEntity<?> decrementCartItem(@PathVariable("cart_item_id") Long cartItemId, HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.decreaseCartItem(cartItemId, userId));
    }

    //장바구니 비우기
    @GetMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    //장바구니 조회
    @GetMapping
    public ResponseEntity<?> getMyCart(HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(cartService.getMyCart(userId));
    }

    /**
     * 주문 서비스 요청 API
     */

    //장바구니 조회
    @GetMapping("/get-items")
    public ResponseEntity<List<CreateOrderReqDto>> getCartItems(@RequestHeader("X-Claim-userId") Long userId){
        return ResponseEntity.ok(cartService.getCartItemsForOrder(userId));
    }

    //장바구니 삭제
    @GetMapping("/clear/after-order")
    public ResponseEntity<?> clearCartAfterOrder(@RequestHeader("X-Claim-userId") Long userId){
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

}
