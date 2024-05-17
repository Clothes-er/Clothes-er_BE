package com.yooyoung.clotheser.user.service;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.jwt.JwtProvider;
import com.yooyoung.clotheser.user.domain.*;
import com.yooyoung.clotheser.user.dto.*;
import com.yooyoung.clotheser.user.repository.*;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BodyShapeRepository bodyShapeRepository;
    private final FavClothesRepository favClothesRepository;
    private final FavStyleRepository favStyleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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
        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
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

    // 로그인
    public LoginResponse login(LoginRequest loginRequest) throws BaseException {

        // 먼저 이메일로 회원 존재 확인
        User user = userRepository.findByEmailAndDeletedAtNull(loginRequest.getEmail())
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER, NOT_FOUND));
        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BaseException(LOGIN_MISMATCH, BAD_REQUEST);
        }

        // 토큰 생성
        TokenResponse tokenResponse = jwtProvider.createToken(user.getId());

        // DB에 Refresh Token 있는지 확인
        RefreshToken preRefreshToken = refreshTokenRepository.findByUserId(user.getId());

        // - 있으면 업데이트
        if (preRefreshToken != null) {
            refreshTokenRepository.save(preRefreshToken.updateRefreshToken(tokenResponse.getRefreshToken()));
        }
        // - 없으면 새로 저장
        else {
            RefreshToken newToken = RefreshToken.builder()
                    .userId(user.getId())
                    .token(tokenResponse.getRefreshToken())
                    .build();
            refreshTokenRepository.save(newToken);
        }

        return new LoginResponse(user.getEmail(), user.getIsFirstLogin(), tokenResponse);
    }

    // TODO: 액세스 토큰 재발급
    /*public TokenResponse refreshToken(String refreshToken) throws BaseException {

        // refresh token이 만료되었거나 없는 경우
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new BaseException(INVALID_JWT_TOKEN, NOT_FOUND);
        }

        // DB에서 refresh token 가져오기
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BaseException(INVALID_JWT_TOKEN, NOT_FOUND));

        Long userId = rt.getUserId();
        return jwtProvider.createToken(userId);
    }*/

    // 최초 로그인
    public FirstLoginResponse firstLogin(FirstLoginRequest firstLoginRequest, User user) throws BaseException {

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

    // 로그아웃

    // 회원 프로필 조회
    public UserProfileResponse getProfile(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 체형, 카테고리, 스타일 추가
        List<String> bodyShapes = bodyShapeRepository.findAllByUserId(user.getId())
                .stream()
                .map(BodyShape::getShape)
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> categories = favClothesRepository.findAllByUserId(user.getId())
                .stream()
                .map(FavClothes::getCategory)
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> styles = favStyleRepository.findAllByUserId(user.getId())
                .stream()
                .map(FavStyle::getStyle)
                .collect(Collectors.toCollection(ArrayList::new));

        return new UserProfileResponse(user, bodyShapes, categories, styles);
    }
}
