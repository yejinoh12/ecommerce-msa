package com.productservice.entity;

import com.common.entity.BaseEntity;
import com.common.exception.BaseBizException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Setter
    @Column(nullable = false)
    private int stock;

    @Setter
    @Column(nullable = false)
    private int likeCount;

    //상품 구매 가능 시간
    private LocalDateTime startTime;

    // 현재 재고가 있는지 확인
    public boolean hasStock() {
        return this.stock > 0;
    }

    // 요청된 수량에 대해 재고가 충분한지 확인
    public boolean hasEnoughStock(int quantity) {
        return this.stock >= quantity;
    }

    // 판매시간 검증
    public boolean isSaleTimeActive(LocalDateTime currentTime) {
        return currentTime.isAfter(startTime) || currentTime.isEqual(startTime);
    }

    //재고가 1개 이상 있고, 구매 가능 시간인 경우
    public boolean isAvailable(LocalDateTime currentTime) {
        return hasStock() && isSaleTimeActive(currentTime);
    }

    //재고 감소
    public void decreaseStock(int quantity) {

        if (!isSaleTimeActive(LocalDateTime.now())) {
            throw new BaseBizException("구매 가능 시간이 아닙니다.");
        }

        if (!hasEnoughStock(quantity)) {
            throw new BaseBizException("재고 부족으로 주문에 실패했습니다.");
        }

        this.stock -= quantity;
    }

    //재고 증가
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

}
