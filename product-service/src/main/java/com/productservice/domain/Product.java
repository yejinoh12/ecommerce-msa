package com.productservice.domain;

import com.common.exception.BaseBizException;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**********************************************************
 * 모든 상품을 처리하는 통합 엔티티
 **********************************************************/
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int price;

    @Setter
    @Column(nullable = false)
    private int stock;

    @Column
    private LocalDateTime eventStartTime; // 이벤트 시작 시간

    @Column
    private LocalDateTime eventEndTime;   // 이벤트 종료 시간

    @Builder
    public Product(String productName, int price, int stock, LocalDateTime eventStartTime, LocalDateTime eventEndTime) {
        this.productName = productName;
        this.price = price;
        this.stock = stock;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
    }

    public boolean canPurchase(int quantity) {
        if (eventStartTime != null && eventEndTime != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(eventStartTime) || now.isAfter(eventEndTime)) {
                return false;
            }
        }
        return stock >= quantity;
    }

    public void decreaseStock(int quantity) {

        if (!canPurchase(quantity)) {
            throw new BaseBizException("이벤트가 활성 상태가 아닙니다.");
        }
        if (this.stock < quantity) {
            throw new BaseBizException("재고가 부족으로 주문에 실패했습니다.");
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
