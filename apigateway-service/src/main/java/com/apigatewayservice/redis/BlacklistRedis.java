package com.apigatewayservice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlacklistRedis {

    private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";

    public boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
