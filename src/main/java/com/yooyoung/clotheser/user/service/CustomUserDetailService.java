package com.yooyoung.clotheser.user.service;

import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    // TODO: 주어진 토큰으로 DB에서 회원을 찾을 수 없을 때 JSON으로 응답하게끔 리팩토링
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByIdAndDeletedAtNull(Long.parseLong(username))
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));
        return new CustomUserDetails(user);
    }
}
