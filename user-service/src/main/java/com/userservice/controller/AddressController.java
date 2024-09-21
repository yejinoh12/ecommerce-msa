package com.userservice.controller;

import com.common.dto.user.AddressResDto;
import com.common.utils.ParseRequestUtil;
import com.userservice.dto.AddressAddReqDto;
import com.userservice.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/address")
public class AddressController {

    private final AddressService addressService;

    //배송지 추가
    @PostMapping("/add")
    public ResponseEntity<?> addAddress(@RequestBody AddressAddReqDto addressAddReqDto, HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(addressService.addAddress(addressAddReqDto, userId));
    }

    //모든 배송지 조회
    @GetMapping("/all")
    public ResponseEntity<?> getAllAddresses(HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(addressService.getAllAddresses(userId));
    }

    //기본 배송지를 해당 주소로 변경
    @PutMapping("/set/default/{addressId}")
    public ResponseEntity<?> updateDefaultAddress(@PathVariable("addressId") Long addressId, HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(addressService.updateDefaultAddress(userId, addressId));
    }

    //배송지 삭제
    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId, HttpServletRequest request) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(addressService.deleteAddress(addressId, userId));
    }

    //주문 서비스에서 사용자 기본 배송지 조회
    @GetMapping("/default")
    public ResponseEntity<AddressResDto> getDefaultAddress(@RequestHeader("X-Claim-userId") Long userId){
        return ResponseEntity.ok(addressService.getDefaultAddress(userId));
    }

}
