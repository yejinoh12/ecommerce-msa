package com.orderservice.entity;

import com.common.entity.BaseEntity;
import com.orderservice.entity.statusEnum.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private int amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // 결제 상태

    @Column(nullable = false)
    private String paymentMethod; // 결제 방법 (예: 카드, 계좌 이체 등)

    private Payment(Long id, Order order, int amount, PaymentStatus paymentStatus, String paymentMethod) {
        this.id = id;
        this.order = order;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
    }



}
