package com.productservice.service;

import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.common.dto.order.CreateOrderReqDto;
import com.productservice.domain.cart.Cart;
import com.productservice.domain.cart.CartItem;
import com.productservice.domain.product.ProductOption;
import com.productservice.dto.cart.CartAddDto;
import com.productservice.dto.cart.CartDto;
import com.productservice.dto.cart.CartItemDto;
import com.productservice.repository.cart.CartItemRepository;
import com.productservice.repository.cart.CartRepository;
import com.productservice.repository.product.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ProductOptionRepository productOptionRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    //장바구니 추가
    public ApiResponse<?> addCartItem(CartAddDto cartAddDto, Long userId) {

        ProductOption productOption = productOptionRepository.findById(cartAddDto.getOpt())
                .orElseThrow(() -> new BaseBizException("productOptionID가 " + cartAddDto.getOpt() + "인 상품 옵션을 찾을 수 없습니다."));

        //카트가 없다면 카트 생성
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.createCart(userId)));

        //장바구니에 같은 상품옵션이 없다면 추가 가능
        CartItem cartItem = cartItemRepository.findByCartAndProductOption(cart, productOption).orElse(null);

        if (cartItem != null) {
            throw new BaseBizException("장바구니에 이미 해당 상품이 존재합니다.");
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .productOption(productOption)
                    .count(cartAddDto.getCnt())
                    .build();
        }

        cartItemRepository.save(cartItem);
        return getMyCart(userId);
    }

    //장바구니에서 수량 증가
    public ApiResponse<?> incrementCartItem(Long cartItemId, Long userId) {

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BaseBizException("cartItemID가 " + cartItemId + "인 장바구니 아이템을 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 장바구니를 찾을 수 없습니다."));

        //장바구니 검증
        if (!cartItem.getCart().equals(cart)) {
            throw new BaseBizException("장바구니에 해당 상품이 없습니다.");
        }

        cartItem.addCount(1);
        cartItemRepository.save(cartItem);

        return getMyCart(userId);
    }

    //장바구니 수량 감소
    public ApiResponse<CartDto> decreaseCartItem(Long cartItemId, Long userId) {

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BaseBizException("cartItemID가 " + cartItemId + "인 장바구니 아이템을 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 장바구니를 찾을 수 없습니다."));

        //장바구니 검증
        if (!cartItem.getCart().equals(cart)) {
            throw new BaseBizException("장바구니에 해당 상품이 없습니다.");
        }

        //수량이 1보다 크다면 1감소, 아니라면 삭제
        if (cartItem.getCount() > 1) {
            cartItem.subCount(1);
            cartItemRepository.save(cartItem);
        }else{

            cartItemRepository.delete(cartItem);
        }
        return getMyCart(userId);
    }

    //장바구니 전체 삭제
    public ApiResponse<CartDto> clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 장바구니를 찾을 수 없습니다."));

        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.delete(cart);

        return ApiResponse.ok(200, "주문 취소 성공", null);
    }

    //장바구니 조회
    @Transactional(readOnly = true)
    public ApiResponse<CartDto> getMyCart(Long userId) {

        //카트가 없다면 빈 카트 반환
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.createCart(userId));

        List<CartItemDto> cartItemDtos = cartItemRepository.findByCartIdWithProductGroup(cart.getId()).stream()
                .map(item -> CartItemDto.builder()
                        .c_item_id(item.getId())
                        .p_id(item.getProductOption().getId())
                        .p_name(item.getProductOption().getProduct().getProductGroup().getGroupName() + "-" + item.getProductOption().getProduct().getTag())
                        .price(item.getProductOption().getProduct().getProductGroup().getPrice())
                        .opt(Map.of(item.getProductOption().getId(), item.getProductOption().getOptionName()))
                        .cnt(item.getCount())
                        .build())

                .collect(Collectors.toList());

        CartDto cartDto = CartDto.builder()
                .c_id(cart.getId())
                .items(cartItemDtos)
                .build();

        return ApiResponse.ok(200,"장바구니 조회 성공", cartDto);
    }

    //장바구니 조회 및 삭제 (주문 서비스 요청)
    public List<CreateOrderReqDto> getCartItemsForOrder(Long userId) {

        log.info("order-service 장바구니 조회/삭제 요청 시작");
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 장바구니를 찾을 수 없습니다."));

        List<CartItem> cartItems = cartItemRepository.findByCartIdWithProductGroup(cart.getId());

        List<CreateOrderReqDto> orderReqDtos = cartItems.stream()
                .map(item -> new CreateOrderReqDto(
                        item.getProductOption().getId(),
                        item.getCount(),
                        item.getProductOption().getProduct().getProductGroup().getPrice()
                ))
                .collect(Collectors.toList());

        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.delete(cart);
        log.info("order-service 장바구니 조회/삭제 완료");

        return orderReqDtos;
    }

}
