package com.productservice.domain.product;

import com.common.exception.BaseBizException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("EVENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventProduct extends Product {

    private LocalDateTime eventStartTime; // 이벤트 시작 시간
    private LocalDateTime eventEndTime;   // 이벤트 종료 시간

    @Builder
    public EventProduct(String productName, int price, int stock,
                        LocalDateTime eventStartTime, LocalDateTime eventEndTime) {
        super(productName, price, stock);
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
    }

    @Override
    public boolean canPurchase(int quantity) {
        return LocalDateTime.now().isAfter(eventStartTime) &&
                LocalDateTime.now().isBefore(eventEndTime) &&
                super.canPurchase(quantity);
    }

    @Override
    public void decreaseStock(int quantity) {
        if (!canPurchase(quantity)) {
            throw new BaseBizException("이벤트가 활성 상태가 아니거나 재고가 부족합니다.");
        }
        super.decreaseStock(quantity);
    }
}
