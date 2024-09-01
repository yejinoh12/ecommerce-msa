package com.userservice.security;


import com.userservice.entity.User;
import com.userservice.repository.UserRepository;
import com.userservice.util.AesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j(topic = "[UserDetailsServiceImpl] 유저 검증")
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final AesUtil aesUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(aesUtil.encrypt(username))
                .orElseThrow(() -> new UsernameNotFoundException("Not Found " + username));

        return new UserDetailsImpl(user);
    }

    public UserDetailsImpl loadUserByUserId(String userId) throws UsernameNotFoundException {

        log.info("사용자 ID = {} ", userId);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException("Not Found " + userId));

        log.info("사용자 ID로 사용자 객체 조회, 사용자 ID = {}", user.getId().toString());

        return new UserDetailsImpl(user);
    }
}