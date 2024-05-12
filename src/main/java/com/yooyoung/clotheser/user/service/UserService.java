package com.yooyoung.clotheser.user.service;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.user.domain.BodyShape;
import com.yooyoung.clotheser.user.domain.FavClothes;
import com.yooyoung.clotheser.user.domain.FavStyle;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.dto.FirstLoginRequest;
import com.yooyoung.clotheser.user.dto.FirstLoginResponse;
import com.yooyoung.clotheser.user.dto.SignUpRequest;
import com.yooyoung.clotheser.user.dto.SignUpResponse;
import com.yooyoung.clotheser.user.repository.BodyShapeRepository;
import com.yooyoung.clotheser.user.repository.FavClothesRepository;
import com.yooyoung.clotheser.user.repository.FavStyleRepository;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class UserService {

    /*
        내가 설정한 EncrypterConfig 파일을 호출하여 사용해도 되지만,
        Spring에서는 기존 BCryptPasswordEncoder 클래스를 DI 하겠다고 선언하면
        알아서 해당 설정 Bean파일인 EncrypterConfig와 매칭시켜서 사용할 수 있게 해줌
    */
    private final BCryptPasswordEncoder encoder;

    private final UserRepository userRepository;
    private final BodyShapeRepository bodyShapeRepository;
    private final FavClothesRepository favClothesRepository;
    private final FavStyleRepository favStyleRepository;

    // 회원가입
    public SignUpResponse signUp(SignUpRequest signUpRequest) throws BaseException {

        // 중복 확인
        // - 닉네임
        if (userRepository.existsByNicknameAndDeletedAtNull(signUpRequest.getNickname())) {
            throw new BaseException(NICKNAME_EXISTS, CONFLICT);
        }
        // - 이메일 => TODO: 이메일 인중
        if (userRepository.existsByEmailAndDeletedAtNull(signUpRequest.getEmail())) {
            throw new BaseException(EMAIL_EXISTS, CONFLICT);
        }
        // - 전화번호 => TODO: 전화번호 인증
        if (userRepository.existsByPhoneNumberAndDeletedAtNull(signUpRequest.getPhoneNumber())) {
            throw new BaseException(PHONE_NUMBER_EXISTS, CONFLICT);
        }

        // 비밀번호 암호화
        String encodedPassword = encoder.encode(signUpRequest.getPassword());
        User user = signUpRequest.toEntity(encodedPassword);

        return new SignUpResponse(userRepository.save(user));
    }

    // 닉네임 중복 확인
    public BaseResponseStatus checkNickname(String nickname) throws BaseException {
        if (userRepository.existsByNicknameAndDeletedAtNull(nickname)) {
            throw new BaseException(NICKNAME_EXISTS, CONFLICT);
        }
        return SUCCESS;
    }

    // 최초 로그인
    public FirstLoginResponse firstLogin(FirstLoginRequest firstLoginRequest) throws BaseException {

        // TODO: SpringSecurity 적용해서 유저 받아오기
        // 회원가입한 유저 존재 확인
        User user = userRepository.findByIdAndDeletedAtNull(firstLoginRequest.getUserId())
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER, NOT_FOUND));

        // 최초 로그인이 맞는지 확인
        if (!user.getIsFirstLogin()) {
            throw new BaseException(IS_NOT_FIRST_LOGIN, FORBIDDEN);
        }

        // 주소, 성별, 키, 몸무게, 발 사이즈 추가
        User updatedUser = user.firstLogin(firstLoginRequest);
        userRepository.save(updatedUser);

        // 체형, 카테고리, 스타일 추가
        List<String> bodyShapes = firstLoginRequest.getBodyShapes();
        if (bodyShapes != null) {
            for (String shape : bodyShapes) {
                BodyShape bodyShape = BodyShape.builder()
                        .user(updatedUser)
                        .shape(shape)
                        .build();
                bodyShapeRepository.save(bodyShape);
            }
        }
        List<String> categories = firstLoginRequest.getCategories();
        if (categories != null) {
            for (String category : categories) {
                FavClothes favClothes = FavClothes.builder()
                        .user(updatedUser)
                        .category(category)
                        .build();
                favClothesRepository.save(favClothes);
            }
        }
        List<String> styles = firstLoginRequest.getStyles();
        if (styles != null) {
            for (String style : styles) {
                FavStyle favStyle = FavStyle.builder()
                        .user(updatedUser)
                        .style(style)
                        .build();
                favStyleRepository.save(favStyle);
            }
        }

        return new FirstLoginResponse(updatedUser, bodyShapes, categories, styles);

    }
}
