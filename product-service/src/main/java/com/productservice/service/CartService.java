package com.productservice.service;

import com.common.dto.product.CartResDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.productservice.domain.Cart;
import com.productservice.domain.CartItem;
import com.productservice.domain.Product;
import com.productservice.dto.cart.CartAddDto;
import com.productservice.dto.cart.CartDto;
import com.productservice.dto.cart.CartItemDto;
import com.productservice.repository.CartItemRepository;
import com.productservice.repository.CartRepository;
import com.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    // 장바구니 추가
    public ApiResponse<?> addCartItem(CartAddDto cartAddDto, Long userId) {

        Product product = findProduct(cartAddDto.getProductId());

        //재고 검증
        if (!product.hasSufficientStock(cartAddDto.getCnt())) {
            throw new BaseBizException("재고가 부족합니다. 현재 재고: " + product.getStock());
        }

        //이벤트 시간 검증
        if (!product.isSaleTimeActive(LocalDateTime.now())) {
            throw new BaseBizException("구매 가능 시간이 아닙니다. " + product.getStartTime() + "부터 구매 가능합니다.");
        }

        //카트 검증
        Cart cart = getOrCreateCart(userId);
        validateCartItemNotExists(cart, product);

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .count(cartAddDto.getCnt())
                .build();

        cartItemRepository.save(cartItem);
        return getMyCart(userId);
    }

    // 장바구니에서 수량 증가
    public ApiResponse<?> incrementCartItem(Long cartItemId, Long userId) {

        CartItem cartItem = getCartItem(cartItemId);
        Cart cart = getCart(userId);

        validateCartOwnership(cartItem, cart);

        //재고 검증
        Product product = cartItem.getProduct();
        if (!product.hasSufficientStock(cartItem.getCount() + 1)){
            throw new BaseBizException("재고가 부족합니다. 현재 재고: " + product.getStock());
        }

        cartItem.addCount(1);
        cartItemRepository.save(cartItem);

        return getMyCart(userId);
    }

    // 장바구니 수량 감소
    public ApiResponse<CartDto> decreaseCartItem(Long cartItemId, Long userId) {

        CartItem cartItem = getCartItem(cartItemId);
        Cart cart = getCart(userId);

        validateCartOwnership(cartItem, cart);

        if (cartItem.getCount() > 1) {
            cartItem.subCount(1);
            cartItemRepository.save(cartItem);
        } else {
            cartItemRepository.delete(cartItem);
        }

        return getMyCart(userId);
    }

    // 장바구니 전체 삭제
    public ApiResponse<?> clearCart(Long userId) {
        Cart cart = getCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.delete(cart);
        return ApiResponse.ok(200, "장바구니 상품이 모두 삭제되었습니다.", null);
    }

    // 장바구니 조회
    @Transactional(readOnly = true)
    public ApiResponse<CartDto> getMyCart(Long userId) {

        Cart cart = getCart(userId);

        List<CartItemDto> cartItemDtos = cartItemRepository.findByCartId(cart.getId()).stream()
                .map(item -> CartItemDto.builder()
                        .c_item_id(item.getId())
                        .name(item.getProduct().getName())
                        .price(item.getProduct().getPrice() * item.getCount()) // 단가 * 수량
                        .cnt(item.getCount())
                        .hasStock(item.getProduct().hasStock())
                        .isInSaleTime(item.getProduct().isSaleTimeActive(LocalDateTime.now()))
                        .build())
                .collect(Collectors.toList());

        // 총 가격 계산
        int totalPrice = cartItemDtos.stream()
                .mapToInt(CartItemDto::getPrice)
                .sum();

        // 장바구니 DTO 생성
        CartDto cartDto = CartDto.builder()
                .totalPrice(totalPrice) // 총 가격 설정
                .items(cartItemDtos) // 아이템 리스트 설정
                .build();

        return ApiResponse.ok(200, "장바구니 조회 성공", cartDto);
    }


    //주문 서비스에서 장바구니 조회
    public List<CartResDto> getCartItemsForOrder(Long userId) {

        log.info("order-service 장바구니 조회 요청, userId={}", userId);

        Cart cart = getCart(userId);

        List<CartResDto> cartResDtos = cartItemRepository.findByCartId(cart.getId())
                .stream()
                .map(item -> CartResDto.builder()
                        .productId(item.getProduct().getId())
                        .unitPrice(item.getProduct().getPrice())
                        .cnt(item.getCount())
                        .build())
                .collect(Collectors.toList());

        log.info("order-service 장바구니 조회 완료");

        return cartResDtos;
    }


    //검증 메서드
    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("productID가 " + productId + "인 상품 옵션을 찾을 수 없습니다."));
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.createCart(userId)));
    }

    private Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseBizException("장바구니가 비었습니다. userID:  " + userId));
    }

    private CartItem getCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BaseBizException("cartItemID가 " + cartItemId + "인 장바구니 아이템을 찾을 수 없습니다."));
    }

    //장바구니에 상품을 담을 때는, 장바구니에 해당 상품이 없어야 함
    private void validateCartItemNotExists(Cart cart, Product product) {
        if (cartItemRepository.findByCartAndProduct(cart, product).isPresent()) {
            throw new BaseBizException("장바구니에 이미 해당 상품이 존재합니다.");
        }
    }

    //수량 증감 시 장바구니에 해당 상품이 있는지 확인하기 위해 사용
    private void validateCartOwnership(CartItem cartItem, Cart cart) {
        if (!cartItem.getCart().equals(cart)) {
            throw new BaseBizException("장바구니에 해당 상품이 없습니다.");
        }
    }
}
