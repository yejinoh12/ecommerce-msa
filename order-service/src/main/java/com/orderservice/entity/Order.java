package com.orderservice.entity;

import com.common.entity.BaseEntity;
import com.orderservice.entity.statusEnum.DeliveryStatus;
import com.orderservice.entity.statusEnum.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    private Order(Long userId, int totalPrice, OrderStatus orderStatus, DeliveryStatus deliveryStatus) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.deliveryStatus = deliveryStatus;
    }

    public static Order createOrder(Long userId, int totalPrice) {
        return new Order(userId, totalPrice, OrderStatus.ORDERED, DeliveryStatus.PENDING);
    }

    /**********************************************************
     * 상태 변경 메서드
     **********************************************************/

    public void updateStatusToCanceled() {
        this.orderStatus = OrderStatus.CANCELED;
        this.deliveryStatus = DeliveryStatus.CANCELED;
    }

    public void updateStatusToReturning() {
        this.orderStatus = OrderStatus.RETURN_ING;
        this.deliveryStatus = DeliveryStatus.CANCELED;
    }

    public void updateStatusToReturned() {
        this.orderStatus = OrderStatus.RETURNED;
    }

}
