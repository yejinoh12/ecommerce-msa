package com.userservice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisBlacklistService {

    private final StringRedisTemplate blacklistRedisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";

    // 블랙리스트에 토큰 추가
    public void addTokenToBlacklist(String token, long expirationTimeInSeconds) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        blacklistRedisTemplate.opsForValue().set(key, token);
        blacklistRedisTemplate.expire(key, expirationTimeInSeconds, TimeUnit.SECONDS);
    }

    // 블랙리스트에서 토큰 확인
    public boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(blacklistRedisTemplate.hasKey(key));
    }
}
