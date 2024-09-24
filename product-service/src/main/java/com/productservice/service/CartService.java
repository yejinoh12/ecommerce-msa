package com.productservice.service;

import com.common.dto.user.AddressResDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.productservice.client.UserServiceClient;
import com.productservice.dto.cart.CartOrderResDto;
import com.productservice.entity.Cart;
import com.productservice.entity.CartItem;
import com.productservice.entity.Product;
import com.productservice.dto.cart.CartAddReqDto;
import com.productservice.dto.cart.CartResDto;
import com.productservice.dto.cart.CartItemResDto;
import com.productservice.repository.CartItemRepository;
import com.productservice.repository.CartRepository;
import com.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final UserServiceClient userServiceClient;

    // 장바구니 추가
    @Transactional
    public ApiResponse<?> addCartItem(CartAddReqDto cartAddReqDto, Long userId) {

        //상품 검증
        Long productId = cartAddReqDto.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("productID가 " + productId + "인 상품 옵션을 찾을 수 없습니다."));

        //재고 검증
        if (!product.hasEnoughStock(cartAddReqDto.getCnt())) {
            throw new BaseBizException("재고가 부족합니다. 현재 재고: " + product.getStock());
        }

        //판매 시작 시간 검증
        if (!product.isSaleTimeActive(LocalDateTime.now())) {
            throw new BaseBizException("구매 가능 시간이 아닙니다. " + product.getStartTime() + "부터 구매 가능합니다.");
        }

        //카트가 없다면 새로 생성
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.createCart(userId)));

        //장바구니에 있는 상품은 담을 수 없음
        if (cartItemRepository.findByCartAndProduct(cart, product).isPresent()) {
            throw new BaseBizException("장바구니에 이미 해당 상품이 존재합니다.");
        }

        //장바구니 상품 생성
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .count(cartAddReqDto.getCnt())
                .build();

        //저장
        cartItemRepository.save(cartItem);

        return viewCartItems(userId);
    }

    // 장바구니에서 수량 증가
    @Transactional
    public ApiResponse<?> incrementCartItem(Long cartItemId, Long userId) {

        CartItem cartItem = getCartItem(cartItemId);
        Cart cart = getCart(userId);
        validateCartOwnership(cartItem, cart);

        //재고 검증
        Product product = cartItem.getProduct();
        if (!product.hasEnoughStock(cartItem.getCount() + 1)) {
            throw new BaseBizException("재고가 부족합니다. 현재 재고: " + product.getStock());
        }

        cartItem.addCount(1);
        cartItemRepository.save(cartItem);

        return viewCartItems(userId);
    }

    // 장바구니 수량 감소
    @Transactional
    public ApiResponse<CartResDto> decreaseCartItem(Long cartItemId, Long userId) {

        CartItem cartItem = getCartItem(cartItemId);
        Cart cart = getCart(userId);

        validateCartOwnership(cartItem, cart);

        if (cartItem.getCount() > 1) {
            cartItem.subCount(1);
            cartItemRepository.save(cartItem);
        } else {
            cartItemRepository.delete(cartItem);
        }

        return viewCartItems(userId);
    }

    // 장바구니 전체 삭제
    @Transactional
    public ApiResponse<?> clearCart(Long userId) {
        Cart cart = getCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.delete(cart);
        return ApiResponse.ok(200, "장바구니 상품이 모두 삭제되었습니다.", null);
    }

    // 장바구니 조회
    public ApiResponse<CartResDto> viewCartItems(Long userId) {

        Cart cart = getCart(userId);

        List<CartItemResDto> cartItemResDtos =
                cartItemRepository.findByCartId(cart.getId()).stream()
                        .map(item -> CartItemResDto.builder()
                                .cartItemId(item.getId())
                                .productId((item.getProduct().getId()))
                                .name(item.getProduct().getName())
                                .unitPrice(item.getProduct().getPrice())
                                .quantity(item.getCount())
                                .subTotal(item.getProduct().getPrice() * item.getCount())
                                .hasStock(item.getProduct().hasStock())
                                .isInSaleTime(item.getProduct().isSaleTimeActive(LocalDateTime.now()))
                                .build())
                        .collect(Collectors.toList());

        // 총 가격 계산
        int totalPrice = cartItemResDtos.stream()
                .mapToInt(CartItemResDto::getSubTotal)
                .sum();

        // 장바구니 DTO 생성
        CartResDto cartResDto = CartResDto.builder()
                .totalPrice(totalPrice) // 총 가격 설정
                .items(cartItemResDtos) // 아이템 리스트 설정
                .build();

        return ApiResponse.ok(200, "장바구니 조회 성공", cartResDto);
    }

    // 주문 전 장바구니 조회
    public ApiResponse<CartOrderResDto> orderCartItems(Long userId) {

        // 장바구니 조회
        CartResDto cartResDto = viewCartItems(userId).getData();

        // 주소 정보 조회
        AddressResDto address = userServiceClient.getDefaultAddress(userId);

        // 상품 정보
        List<CartItemResDto> orderItems = cartResDto.getItems().stream()
                .filter(Objects::nonNull)  //null 값은 포함하지 않음
                .map(item -> CartItemResDto.builder()
                        .productId(item.getProductId())
                        .name(item.getName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        CartOrderResDto orderReqDto = CartOrderResDto.builder()
                .items(orderItems)
                .address(address)
                .build();

        return ApiResponse.ok(200, "결제를 진행해주세요", orderReqDto);
    }

    //주문 서비스에서 장바구니 조회
    public List<CartItemResDto> getCartItemsForOrder(Long userId) {

        Cart cart = getCart(userId);

        return cartItemRepository.findByCartId(cart.getId())
                .stream()
                .map(item -> CartItemResDto.builder()
                        .productId(item.getProduct().getId())
                        .name(item.getProduct().getName())
                        .unitPrice(item.getProduct().getPrice())
                        .quantity(item.getCount())
                        .build())
                .collect(Collectors.toList());
    }

    //장바구니 조회
    private Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseBizException("장바구니가 비었습니다. userID:  " + userId));
    }

    //장바구니 아이템 조회
    private CartItem getCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BaseBizException("cartItemID가 " + cartItemId + "인 장바구니 아이템을 찾을 수 없습니다."));
    }

    //수량 증감 시 장바구니에 해당 상품이 있는지 확인하기 위해 사용
    private void validateCartOwnership(CartItem cartItem, Cart cart) {
        if (!cartItem.getCart().equals(cart)) {
            throw new BaseBizException("장바구니에 해당 상품이 없습니다.");
        }
    }
}
