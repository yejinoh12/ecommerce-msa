package com.userservice.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailRedisService {

    private final StringRedisTemplate emailRedisTemplate; // 데이터베이스 인덱스 1 사용

    //이메일 인증 코드 저장
    public void setEmailVerificationCode(String email, String code) {
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        hashOperations.put(email, "code", code);
        hashOperations.put(email, "auth", "N");
        emailRedisTemplate.expire(email, Duration.ofMinutes(3));
    }

    //이메일 인증 상태 변경
    public void updateAuthenticationStatus(String email) {
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        hashOperations.put(email, "auth", "Y");
    }

    //이메일 인증 코드 조회
    public String getEmailVerificationCode(String key) {
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        return hashOperations.get(key, "code");
    }

    //이메일 인증 상태 조회
    public String getAuthenticationStatus(String key) {
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        return hashOperations.get(key, "auth");
    }

    //삭제
    public void deleteEmailInfo(String key) {
        HashOperations<String, String, String> hashOperations = emailRedisTemplate.opsForHash();
        hashOperations.delete(key, "code");
        hashOperations.delete(key, "auth");
    }
}
