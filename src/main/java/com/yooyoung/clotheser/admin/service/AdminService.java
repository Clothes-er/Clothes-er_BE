package com.yooyoung.clotheser.admin.service;

import com.yooyoung.clotheser.admin.domain.Report;
import com.yooyoung.clotheser.admin.dto.response.AdminLoginResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportListResponse;
import com.yooyoung.clotheser.admin.repository.ReportRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.jwt.JwtProvider;
import com.yooyoung.clotheser.user.domain.RefreshToken;
import com.yooyoung.clotheser.user.domain.Role;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.dto.request.LoginRequest;
import com.yooyoung.clotheser.user.dto.response.TokenResponse;
import com.yooyoung.clotheser.user.repository.RefreshTokenRepository;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ReportRepository reportRepository;

    // 로그인
    public AdminLoginResponse adminLogin(LoginRequest loginRequest) throws BaseException {

        // 이메일로 회원 존재 확인
        User user = userRepository.findByEmailAndDeletedAtNull(loginRequest.getEmail())
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER_BY_EMAIL, NOT_FOUND));

        // 관리자 확인
        if (user.getIsAdmin() != Role.ADMIN) {
            throw new BaseException(INVALID_USER_JWT, FORBIDDEN);
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BaseException(LOGIN_MISMATCH, BAD_REQUEST);
        }

        // 토큰 생성
        TokenResponse tokenResponse = jwtProvider.createToken(user.getId(), user.getIsAdmin().name());

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

        // 마지막으로 로그인한 시간 업데이트
        if (user.getIsFirstLogin()) {
            user = user.updateIsFirstLogin();
        }
        userRepository.save(user.updateLastLoginAt());

        return new AdminLoginResponse(user, tokenResponse);
    }

    public List<ReportListResponse> getReportList() throws BaseException {

        List<Report> reports = reportRepository.findAllByOrderByIdDesc();
        List<ReportListResponse> responses = new ArrayList<>();
        for (Report report : reports) {
            responses.add(new ReportListResponse(report));
        }

        return responses;
    }
}
