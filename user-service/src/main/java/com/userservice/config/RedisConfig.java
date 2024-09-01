package com.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    private LettuceConnectionFactory createConnectionFactoryWith(int index) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setDatabase(index);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    /**********************************************************
     * 리프레시 토큰 레디스 설정
     **********************************************************/

    @Bean
    @Primary
    LettuceConnectionFactory refreshTokenConnectionFactory() {
        return createConnectionFactoryWith(0);
    }

    @Bean
    StringRedisTemplate refreshTokenRedisTemplate(LettuceConnectionFactory refreshTokenConnectionFactory) {
        return new StringRedisTemplate(refreshTokenConnectionFactory);
    }


    /**********************************************************
     * 이메일 인증 레디스 설정
     **********************************************************/

    @Bean
    @Qualifier("email")
    LettuceConnectionFactory emailConnectionFactory() {
        return createConnectionFactoryWith(1);
    }

    @Bean
    StringRedisTemplate emailRedisTemplate(@Qualifier("email") LettuceConnectionFactory emailConnectionFactory) {
        return new StringRedisTemplate(emailConnectionFactory);
    }

}