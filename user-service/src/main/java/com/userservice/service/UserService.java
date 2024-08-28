package com.userservice.service;

import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.common.dto.user.UserInfoDto;
import com.userservice.entity.User;
import com.userservice.entity.UserRoleEnum;
import com.userservice.dto.SignUpRequestDto;
import com.userservice.dto.UserResponseDto;
import com.userservice.repository.UserRepository;
import com.userservice.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.common.dto.user.UserInfoDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    public ApiResponse<String> signup(SignUpRequestDto signUpRequestDto) {

        String email = AesUtil.encrypt(signUpRequestDto.getEmail());    //이메일 암호화

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BaseBizException("사용중인 이메일입니다.");  //이메일 중복 검증
        }

        //이메일 인증 기능 (구현 예정)
        User user = User.builder()
                .name(AesUtil.encrypt(signUpRequestDto.getName()))
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .address(AesUtil.encrypt(signUpRequestDto.getAddress()))
                .email(AesUtil.encrypt(signUpRequestDto.getEmail()))
                .phoneNumber(AesUtil.encrypt(signUpRequestDto.getPhoneNumber()))
                .role(UserRoleEnum.USER)
                .build();

        userRepository.save(user);

        return ApiResponse.ok(201, "회원 가입 성공", null);
    }

    //회원 조회
    public ApiResponse<UserResponseDto> myPage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 사용자를 찾을 수 없습니다."));

        log.info(user.getPhoneNumber());

        UserResponseDto userResponseDto = UserResponseDto.builder()
                .name(AesUtil.decrypt(user.getName()))
                .email(AesUtil.decrypt(user.getEmail()))
                //.phoneNumber(AesUtil.decrypt(user.getPhoneNumber()))
                .address(AesUtil.decrypt(user.getAddress()))
                .createdAt(user.getCreatedAt())
                .modifiedAt(user.getModifiedAt())
                .build();

        return ApiResponse.ok(200,"회원 조회 성공", userResponseDto);
    }

    //주문 서비스에서 유저 정보 조회
    public UserInfoDto getUserInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 사용자를 찾을 수 없습니다."));

        log.info(user.getPhoneNumber());

        return builder()
                .name(AesUtil.decrypt(user.getName()))
                .email(AesUtil.decrypt(user.getEmail()))
                //.phoneNumber("dd")
                .address(AesUtil.decrypt(user.getAddress()))
                .build();
    }

}