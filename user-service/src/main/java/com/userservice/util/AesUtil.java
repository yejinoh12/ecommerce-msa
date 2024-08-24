package com.userservice.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AesUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "kYUg0bHThU8ZG5bB8ztmKw=="; // Base64 encoded 16 bytes (128 bits)
    private static final String IV = "M4hzA1Vf5k7pQ4Ih6eKXbQ=="; // Base64 encoded 16 bytes (128 bits)

    // AES encryption
    public static String encrypt(String input) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
            byte[] ivBytes = Base64.getDecoder().decode(IV);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    // AES decryption
    public static String decrypt(String encryptedInput) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
            byte[] ivBytes = Base64.getDecoder().decode(IV);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedInput);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}