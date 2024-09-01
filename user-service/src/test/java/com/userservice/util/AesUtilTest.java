package com.userservice.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.keygen.KeyGenerators;

import static org.junit.jupiter.api.Assertions.*;

class AesUtilTest {

    @Test
    public void createKey(){
         String salt = KeyGenerators.string().generateKey();
        System.out.println(salt);
    }

}