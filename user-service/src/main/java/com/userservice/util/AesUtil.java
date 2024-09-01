package com.userservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AesUtil {

    private final AesBytesEncryptor aesBytesEncryptor;

    public String encrypt(String input) {
        try {
            byte[] encryptedBytes = aesBytesEncryptor.encrypt(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("암호화 중 오류가 발생했습니다.", e);
        }
    }

    public String decrypt(String input) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            byte[] decryptedBytes = aesBytesEncryptor.decrypt(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("복호화 중 오류가 발생했습니다.", e);
        }
    }
}