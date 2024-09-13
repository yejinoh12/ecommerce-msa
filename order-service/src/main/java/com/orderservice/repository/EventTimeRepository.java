package com.orderservice.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Repository
public class EventTimeRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String EVENT_START_TIME_KEY_PREFIX = "product:eventStart:";

    public EventTimeRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 상품 ID와 이벤트 시작 시간을 저장
    public void setProductEventStartTime(Long productId, LocalDateTime startTime) {
        String key = EVENT_START_TIME_KEY_PREFIX + productId;
        String value = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        redisTemplate.opsForValue().set(key, value);
    }

    // 상품 ID에 대한 이벤트 시작 시간 조회
    public LocalDateTime getProductEventStartTime(Long productId) {
        String key = EVENT_START_TIME_KEY_PREFIX + productId;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
