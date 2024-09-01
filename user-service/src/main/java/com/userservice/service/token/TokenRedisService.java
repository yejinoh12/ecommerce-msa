package com.userservice.service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenRedisService {

    private final StringRedisTemplate refreshTokenRedisTemplate; // 데이터베이스 인덱스 0 사용

    //토큰 저장
    public void setRefreshToken(String userId, String token) {
        refreshTokenRedisTemplate.opsForValue().set(userId, token);
    }

    //토큰 조회
    public String getRefreshToken(String userId) {
        return refreshTokenRedisTemplate.opsForValue().get(userId);
    }

    //토큰 삭제
    public void deleteRefreshToken(String userId) {
        refreshTokenRedisTemplate.delete(userId);
    }
}
