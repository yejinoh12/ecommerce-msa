package com.orderservice.entity;

/**
 * 주문 완료 후 D+1에 배송중, D+2에 배송완료가 된다고 가정
 * 상품 취소는 배송 준비 중일 때만 가능하고, 취소 후에는 재고가 복구 되며, 주문 상태가 취소 완료가 된다.
 * 반품은 배송 완료 후 D+1까지만 반품이 가능하고(배송 완료가 된 상품에 대해서만 반품 가능) 반품 신청 후 D+1에 재고에 반영.
 */

public enum DeliveryStatus {
    PENDING,
    SHIPPED,
    DELIVERED,
    CANCELED,
}