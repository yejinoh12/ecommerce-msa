package com.orderservice.config;

import com.orderservice.exception.FeignErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Bean
    public FeignErrorDecoder commonFeignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}