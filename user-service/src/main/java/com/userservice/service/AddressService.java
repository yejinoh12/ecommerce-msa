package com.userservice.service;

import com.common.dto.user.AddressResDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.userservice.dto.AddressAddReqDto;
import com.userservice.entity.Address;
import com.userservice.entity.User;
import com.userservice.repository.AddressRepository;
import com.userservice.repository.UserRepository;
import com.userservice.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AesUtil aesUtil;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    //배송지 추가
    @Transactional
    public ApiResponse<?> addAddress(AddressAddReqDto addressAddReqDto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 사용자를 찾을 수 없습니다."));

        // 등록한 배송지를 기본 배송지로 설정한다면, 기존의 기본 배송지 비활성화
        if (addressAddReqDto.isDefault()) {
            addressRepository.updatePreviousDefaultAddressToFalse(userId);
        }

        Address address = Address.builder()
                .user(user)
                .alias(addressAddReqDto.getAlias())
                .postalCode(aesUtil.encrypt(addressAddReqDto.getPostalCode()))
                .address(aesUtil.encrypt(addressAddReqDto.getAddress()))
                .detailAddress(aesUtil.encrypt(addressAddReqDto.getDetailAddr()))
                .phone(aesUtil.encrypt(addressAddReqDto.getPhone()))
                .isDefault(addressAddReqDto.isDefault())
                .build();

        addressRepository.save(address);
        return ApiResponse.ok(200, "배송지 추가 성공", null);
    }

    //모든 배송지 조회
    public ApiResponse<List<AddressResDto>> getAllAddresses(Long userId) {

        List<Address> addresses = addressRepository.findAllByUserIdOrderByIsDefaultDesc(userId);

        List<AddressResDto> addressResDtos = addresses.stream()
                .map(address -> AddressResDto.builder()
                        .addressId(address.getId())
                        .alias(address.getAlias())
                        .address(aesUtil.decrypt(address.getAddress()))
                        .detailAddress(aesUtil.decrypt(address.getDetailAddress()))
                        .phone(aesUtil.decrypt(address.getPhone()))
                        .isDefault(address.isDefault())
                        .build())
                .toList();

        return ApiResponse.ok(200, "배송지 목록 조회 성공", addressResDtos);
    }

    //기본 배송지 변경
    public ApiResponse<?> updateDefaultAddress(Long userId, Long addressId) {

        Address addressToSetDefault = addressRepository.findById(addressId)
                .orElseThrow(() -> new BaseBizException("배송지를 찾을 수 없습니다."));

        if (!addressToSetDefault.getUser().getId().equals(userId)) {
            throw new BaseBizException("사용자 권한이 없습니다.");
        }

        // 기존의 기본 배송지 비활성화
        addressRepository.updatePreviousDefaultAddressToFalse(userId);

        // 선택된 배송지를 기본 배송지로 설정
        addressToSetDefault.setDefault(true);
        addressRepository.save(addressToSetDefault);

        return getAllAddresses(userId);
    }

    //배송지 삭제
    @Transactional
    public ApiResponse<?> deleteAddress(Long addressId, Long userId) {

        // 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 사용자를 찾을 수 없습니다."));

        // 배송지 확인
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BaseBizException("주소 ID가 " + addressId + "인 배송지를 찾을 수 없습니다."));

        // 사용자 배송지인지 확인
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BaseBizException("해당 배송지는 이 사용자의 것이 아닙니다.");
        }

        // 기본 배송지인지 확인
        if (address.isDefault()) {
            throw new BaseBizException("기본 배송지는 삭제할 수 없습니다.");
        }

        // 배송지 삭제
        addressRepository.delete(address);

        return getAllAddresses(userId);
    }

    //주문 서비스에서 사용자 ID로 기본 배송지 조회
    public AddressResDto getDefaultAddress(Long userId) {

        Address address = addressRepository.findDefaultAddressByUserId(userId)
                .orElseThrow(() -> new BaseBizException("기본 배송지를 찾을 수 없습니다."));

        return AddressResDto.builder()
                .addressId(address.getId())
                .alias(address.getAlias())
                .address(aesUtil.decrypt(address.getAddress()))
                .detailAddress(aesUtil.decrypt(address.getDetailAddress()))
                .phone(aesUtil.decrypt(address.getPhone()))
                .isDefault(address.isDefault())
                .build();
    }
}
