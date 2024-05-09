package com.yooyoung.clotheser.user.service;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.dto.SignUpRequestDto;
import com.yooyoung.clotheser.user.dto.SignUpResponseDto;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    /*
        내가 설정한 EncrypterConfig 파일을 호출하여 사용해도 되지만,
        Spring에서는 기존 BCryptPasswordEncoder 클래스를 DI 하겠다고 선언하면
        알아서 해당 설정 Bean파일인 EncrypterConfig와 매칭시켜서 사용할 수 있게 해줌
    */
    private final BCryptPasswordEncoder encoder;

    // 회원가입
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) throws BaseException {

        // 중복 확인
        // - 닉네임
        if (userRepository.existsByNicknameAndDeletedAtNull(signUpRequestDto.getNickname())) {
            throw new BaseException(NICKNAME_EXISTS, CONFLICT);
        }
        // - 이메일 (이메일 인증 기능 추가 예정)
        if (userRepository.existsByEmailAndDeletedAtNull(signUpRequestDto.getEmail())) {
            throw new BaseException(EMAIL_EXISTS, CONFLICT);
        }
        // - 전화번호 (전화번호 인증 기능 추가 예정)
        if (userRepository.existsByPhoneNumberAndDeletedAtNull(signUpRequestDto.getPhoneNumber())) {
            throw new BaseException(PHONE_NUMBER_EXISTS, CONFLICT);
        }

        // 비밀번호 암호화
        String encodedPassword = encoder.encode(signUpRequestDto.getPassword());
        User user = signUpRequestDto.toEntity(encodedPassword);

        return new SignUpResponseDto(userRepository.save(user));
    }

}
