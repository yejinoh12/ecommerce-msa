package com.productservice.client;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.user.AddressResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "user-service", url = "http://localhost:8000")
public interface UserServiceClient {

    @GetMapping("/user/address/default")
    AddressResDto getDefaultAddress(@RequestHeader("X-Claim-userId") Long userId);

}
