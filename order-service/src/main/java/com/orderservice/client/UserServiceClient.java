package com.orderservice.client;

import com.common.dto.user.AddressResDto;
import com.common.dto.user.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "http://localhost:8000")
public interface UserServiceClient {

    @GetMapping("/user/info")
    UserInfoDto getUserInfo(@RequestHeader("X-Claim-userId") Long userId);

    @GetMapping("/user/default/address")
    AddressResDto getDefaultAddress(@RequestHeader("X-Claim-userId") Long userId);
}
