package com.yooyoung.clotheser.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.jwt.JwtProvider;
import com.yooyoung.clotheser.user.domain.*;
import com.yooyoung.clotheser.user.dto.*;
import com.yooyoung.clotheser.user.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

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

    // 회원 정보 조회
    public UserInfoResponse getUserInfo(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        return new UserInfoResponse(user);
    }


    // 주소 조회
    public AddressResponse getAddress(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        return new AddressResponse(user);

    }

    // 주소 수정
    public AddressResponse updateAddress(User user, AddressRequest addressRequest) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 수정한 주소로 DB에 저장
        User updatedUser = user.updateAddress(addressRequest.getLatitude(), addressRequest.getLongitude());
        userRepository.save(updatedUser);

        return new AddressResponse(updatedUser);

    }

    // 프로필 사진 수정
    public ProfileImageResponse updateProfileImage(User user, MultipartFile profileImage) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 기존 이미지가 있는 경우 S3에서 삭제
        if (user.getProfileUrl() != null) {
            // 객체 key 추출
            String originalImageKey = user.getProfileUrl().substring(user.getProfileUrl().lastIndexOf("/") + 1);
            amazonS3.deleteObject(bucket, "profiles/" + originalImageKey);
        }

        // S3에 새로운 이미지 업로드
        String newProfileImage;
        try {
            String fileName = "profiles/" + UUID.randomUUID() + "_" + profileImage.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(profileImage.getSize());
            metadata.setContentType(profileImage.getContentType());

            amazonS3.putObject(bucket, fileName, profileImage.getInputStream(), metadata);
            newProfileImage = amazonS3.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new BaseException(S3_UPLOAD_ERROR, INTERNAL_SERVER_ERROR);
        }

        // DB에 새로운 S3 URL 저장
        User updatedUser = user.updateProfileUrl(newProfileImage);
        userRepository.save(updatedUser);

        return new ProfileImageResponse(user.getNickname(), user.getEmail(), user.getProfileUrl());
    }

}
