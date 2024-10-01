package com.orderservice.entity;

import com.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    //배송지 필드 추가

    @Column(nullable = false)
    private String addressAlias;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String detailAddress;

    @Column(nullable = false)
    private String phone;


    //상태 변경 메서드들
    public void updateStatusToCanceled() {
        this.orderStatus = OrderStatus.CANCELED;
        this.deliveryStatus = DeliveryStatus.CANCELED;
    }

    public void updateStatusToReturning() {
        this.orderStatus = OrderStatus.RETURN_REQ;
        this.deliveryStatus = DeliveryStatus.CANCELED;
    }

    public void updateStatusToReturned() {
        this.orderStatus = OrderStatus.RETURNED;
    }

}
