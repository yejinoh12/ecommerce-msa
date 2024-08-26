package com.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    //상품 옵션(상품 서비스에서 가져오기)
    @Column(nullable = false)
    private Long productOptionId;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int quantity;

    private OrderItem(Order order, Long productOptionId, int unitPrice, int quantity) {
        this.order = order;
        this.productOptionId = productOptionId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static OrderItem createOrderItem(Order order, Long productOptionId, int unitPrice, int quantity) {
        return new OrderItem(order, productOptionId, unitPrice, quantity);
    }

}
