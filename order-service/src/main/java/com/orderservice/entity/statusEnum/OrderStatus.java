package com.orderservice.entity.statusEnum;

public enum OrderStatus {


    PAYMENT_IN_PROGRESS, //결제 대기 중
    PAYMENT_FAILED,      //결제 실패
    PAYMENT_ABORTED,     //결제 중 고객 이탈

    ORDERED,
    CANCELED,
    RETURN_REQ,
    RETURNED,
    CONFIRMED
}