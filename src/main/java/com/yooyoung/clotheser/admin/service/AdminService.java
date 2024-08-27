package com.yooyoung.clotheser.admin.service;

import com.yooyoung.clotheser.admin.domain.Report;
import com.yooyoung.clotheser.admin.domain.ReportAction;
import com.yooyoung.clotheser.admin.domain.ReportState;
import com.yooyoung.clotheser.admin.dto.request.ReportActionRequest;
import com.yooyoung.clotheser.admin.dto.response.AdminLoginResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportListResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportResponse;
import com.yooyoung.clotheser.admin.repository.ReportRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
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

    /* 관리자 로그인 */
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

    /* 신고 목록 조회 */
    public List<ReportListResponse> getReportList() throws BaseException {

        List<Report> reports = reportRepository.findAllByOrderByIdDesc();
        List<ReportListResponse> responses = new ArrayList<>();
        for (Report report : reports) {
            responses.add(new ReportListResponse(report));
        }

        return responses;
    }

    /* 신고 조회 */
    public ReportResponse getReport(Long reportId) throws BaseException {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_REPORT, NOT_FOUND));

        return new ReportResponse(report);
    }

    /* 신고 처리 */
    public BaseResponseStatus actionReport(Long reportId, ReportActionRequest reportActionRequest) throws BaseException {

        // 신고 불러오기
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_REPORT, NOT_FOUND));

        // 신고 조치는 한 번만 가능
        if (report.getState() == ReportState.ACTIONED) {
            throw new BaseException(REPORT_ACTION_EXISTS, CONFLICT);
        }

        // 조치별 이후 로직
        ReportAction action = reportActionRequest.getAction();
        switch (action) {
            // 1. 이용 제한
            case RESTRICTED -> {
                // 회원 이용 제한 설정
                // -> 로그인 시도 시 "서비스 이용이 제한되었습니다."
                // -> 대여글, 보유 옷 숨김 처리 (목록 응답값에서 제외됨)
                // -> 채팅방 목록, 채팅방 조회에서 isRestricted = true
                User reportee = report.getReportee();
                reportee = reportee.updateIsRestricted();
                userRepository.save(reportee);
            }

            // 2. 옷장 점수 차감
            case DOCKED -> {
                User reportee = report.getReportee();
                reportee = reportee.updateClosetScore(-2);
                userRepository.save(reportee);
            }

            // 3. 무시
            case IGNORED -> {}
        }

        // 신고 조치 내역 변경
        report = report.updateAction(action);
        reportRepository.save(report);

        return SUCCESS;
    }
}
