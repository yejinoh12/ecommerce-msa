package com.userservice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenService {

    private final StringRedisTemplate refreshTokenRedisTemplate;
    protected static final String PREFIX = "refresh:";

    // 토큰 저장
    public void setRefreshToken(String userId, String token) {
        String key = PREFIX + userId;
        refreshTokenRedisTemplate.opsForValue().set(key, token);
    }

    // 토큰 조회
    public String getRefreshToken(String userId) {
        String key = PREFIX + userId;
        return refreshTokenRedisTemplate.opsForValue().get(key);
    }

    // 토큰 삭제
    public void deleteRefreshToken(String userId) {
        String key = PREFIX + userId;
        refreshTokenRedisTemplate.delete(key);
    }
}
