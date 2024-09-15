package com.orderservice.entity;

public enum OrderStatus {

    PAYMENT_IN_PROGRESS, //결제 대기 중
    PAYMENT_FAILED,      //결제 실패

    ORDERED,             //결제 성공 시 주문 완료로 변경
    CANCELED,            //취소
    RETURN_REQ,          //반품 요청
    RETURNED,            //반품 완료
    CONFIRMED            //구매 확정
}