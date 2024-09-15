package com.productservice.redis;

import com.common.exception.BaseBizException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisProduct {

    private Integer stock;
    private String eventStartTime;
    private String eventEndTime;
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public RedisProduct(Integer stock, LocalDateTime eventStartTime, LocalDateTime eventEndTime) {
        this.stock = stock;
        this.eventStartTime = eventStartTime.format(formatter);
        this.eventEndTime = eventEndTime.format(formatter);
    }

    //구매 가능 여부
    public void canPurchase(int quantity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = parseDateTime(eventStartTime);
        LocalDateTime endTime = parseDateTime(eventEndTime);

        // 구매 시간이 유효한지 확인
        if (startTime != null && endTime != null) {
            if (now.isBefore(startTime)) {
                throw new BaseBizException("구매 시작 전입니다. 시작 시간: " + startTime);
            }
            if (now.isAfter(endTime)) {
                throw new  BaseBizException("구매 종료 시간이 지났습니다. 종료 시간: " + endTime);
            }
        }

        // 재고가 충분한지 확인
        if (stock < quantity) {
            throw new  BaseBizException("재고가 부족합니다. 현재 재고: " + stock);
        }
    }

    // 문자열 -> LocalDateTime
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
}
