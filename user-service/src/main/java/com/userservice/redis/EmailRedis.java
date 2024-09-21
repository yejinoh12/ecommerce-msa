package com.userservice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailRedis {

    private final StringRedisTemplate emailRedisTemplate;
    private static final String PREFIX = "email:";

    // 이메일 인증 코드 저장
    public void setEmailVerificationCode(String email, String code) {
        String key = PREFIX + email;
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        hashOperations.put(key, "code", code);
        hashOperations.put(key, "auth", "N");
        emailRedisTemplate.expire(key, Duration.ofMinutes(3));
    }

    // 이메일 인증 상태 변경
    public void updateAuthenticationStatus(String email) {
        String key = PREFIX + email;
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        hashOperations.put(key, "auth", "Y");
    }

    // 이메일 인증 코드 조회
    public String getEmailVerificationCode(String email) {
        String key = PREFIX + email;
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        return hashOperations.get(key, "code");
    }

    // 이메일 인증 상태 조회
    public String getAuthenticationStatus(String email) {
        String key = PREFIX + email;
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        return hashOperations.get(key, "auth");
    }

    // 삭제
    public void deleteEmailInfo(String email) {
        String key = PREFIX + email;
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        hashOperations.delete(key, "code");
        hashOperations.delete(key, "auth");
    }
}
