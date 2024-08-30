package com.productservice.service.cart;

import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.common.dto.order.CreateOrderReqDto;
import com.productservice.domain.cart.Cart;
import com.productservice.domain.cart.CartItem;
import com.productservice.domain.product.Product;
import com.productservice.dto.cart.CartAddDto;
import com.productservice.dto.cart.CartDto;
import com.productservice.dto.cart.CartItemDto;
import com.productservice.repository.cart.CartItemRepository;
import com.productservice.repository.cart.CartRepository;
import com.productservice.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Product product = findProduct(cartAddDto.getP_id());
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
                        .p_id(item.getProduct().getId())
                        .p_name(item.getProduct().getProductName())
                        .price(item.getProduct().getPrice())
                        .cnt(item.getCount())
                        .build())
                .collect(Collectors.toList());

        CartDto cartDto = CartDto.builder()
                .c_id(cart.getId())
                .items(cartItemDtos)
                .build();

        return ApiResponse.ok(200, "장바구니 조회 성공", cartDto);
    }

    /**********************************************************
     * 주문 서비스 요청 API
     **********************************************************/

    //장바구니 조회
    public List<CreateOrderReqDto> getCartItemsForOrder(Long userId) {

        log.info("order-service 장바구니 조회 요청");
        log.info("userId ={}", userId);

        Cart cart = getCart(userId);
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        List<CreateOrderReqDto> orderReqDtos = cartItems.stream()
                .map(item -> new CreateOrderReqDto(
                        item.getProduct().getId(),
                        item.getCount(),
                        item.getProduct().getPrice() * item.getCount()
                ))
                .collect(Collectors.toList());

        log.info("order-service 장바구니 조회 완료");

        return orderReqDtos;
    }

    /**********************************************************
     * 헬퍼 메서드
     **********************************************************/

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
