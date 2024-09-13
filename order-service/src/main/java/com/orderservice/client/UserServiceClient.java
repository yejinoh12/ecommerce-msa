package com.orderservice.client;

import com.common.dto.user.UserInfoDto;
import com.orderservice.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "http://localhost:8000")
public interface UserServiceClient {

    @GetMapping("/user/info-order")
    UserInfoDto getUserInfo(@RequestHeader("X-Claim-userId") Long userId);
}
