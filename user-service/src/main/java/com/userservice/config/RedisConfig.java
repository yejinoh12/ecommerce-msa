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

    @Value("${spring.data.redis.database1}")
    private int databaseIndex1; // 데이터베이스 인덱스 추가

    @Value("${spring.data.redis.database2}")
    private int databaseIndex2; // 데이터베이스 인덱스 추가



    private LettuceConnectionFactory createConnectionFactoryWith(int index) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setDatabase(index);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    @Primary
    LettuceConnectionFactory refreshTokenConnectionFactory() {
        return createConnectionFactoryWith(databaseIndex1);
    }

    @Bean
    StringRedisTemplate refreshTokenRedisTemplate(LettuceConnectionFactory refreshTokenConnectionFactory) {
        return new StringRedisTemplate(refreshTokenConnectionFactory);
    }

    @Bean
    @Qualifier("email")
    LettuceConnectionFactory emailConnectionFactory() {
        return createConnectionFactoryWith(databaseIndex2);
    }

    @Bean
    StringRedisTemplate emailRedisTemplate(@Qualifier("email") LettuceConnectionFactory emailConnectionFactory) {
        return new StringRedisTemplate(emailConnectionFactory);
    }

}