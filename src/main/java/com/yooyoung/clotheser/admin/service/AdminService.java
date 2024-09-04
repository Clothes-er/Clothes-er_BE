package com.yooyoung.clotheser.admin.service;

import com.yooyoung.clotheser.admin.domain.Report;
import com.yooyoung.clotheser.admin.domain.ReportAction;
import com.yooyoung.clotheser.admin.domain.ReportState;
import com.yooyoung.clotheser.admin.dto.request.ReportActionRequest;
import com.yooyoung.clotheser.admin.dto.response.AdminLoginResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportListResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportResponse;
import com.yooyoung.clotheser.admin.dto.response.UserListResponse;
import com.yooyoung.clotheser.admin.repository.ReportRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.jwt.JwtProvider;
import com.yooyoung.clotheser.rental.domain.RentalState;
import com.yooyoung.clotheser.rental.repository.RentalInfoRepository;
import com.yooyoung.clotheser.review.domain.Review;
import com.yooyoung.clotheser.review.repository.ReviewKeywordRepository;
import com.yooyoung.clotheser.review.repository.ReviewRepository;
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

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final RentalInfoRepository rentalInfoRepository;

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

            Long reporteeId = report.getReportee().getId();
            // 거래중 여부 확인
            boolean isRented = rentalInfoRepository.existsByBuyerIdAndStateOrLenderIdAndState(
                    reporteeId, RentalState.RENTED, reporteeId, RentalState.RENTED
            );

            responses.add(new ReportListResponse(report, isRented));
        }

        return responses;
    }

    /* 신고 조회 */
    public ReportResponse getReport(Long reportId) throws BaseException {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_REPORT, NOT_FOUND));

        Long reporteeId = report.getReportee().getId();
        // 거래중 여부 확인
        boolean isRented = rentalInfoRepository.existsByBuyerIdAndStateOrLenderIdAndState(
                reporteeId, RentalState.RENTED, reporteeId, RentalState.RENTED
        );

        return new ReportResponse(report, isRented);
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
            // 1. 유예
            case SUSPENDED -> {
                // -> 대여글, 보유 옷 생성/수정/삭제 제한
                // -> 대여글, 보유 옷 숨김 처리 (목록 응답값에서 제외됨)
                // -> 대여글, 보유 옷, 채팅방, 프로필 조회에서 isSuspended = true
                User reportee = report.getReportee();
                reportee = reportee.updateIsSuspended();
                userRepository.save(reportee);
            }

            // 2. 이용 제한
            case RESTRICTED -> {
                // -> 로그인 시도 시 "서비스 이용이 제한되었습니다."
                // -> 대여글, 보유 옷 숨김 처리 (목록 응답값에서 제외됨)
                // -> 대여글, 채팅방 목록, 채팅방 조회에서 isRestricted = true
                // TODO: 대여글 isRestricted 추가

                boolean isRented = rentalInfoRepository.existsByBuyerIdAndStateOrLenderIdAndState(
                        report.getReportee().getId(), RentalState.RENTED, report.getReportee().getId(), RentalState.RENTED
                );
                if (isRented) {
                    throw new BaseException(REPORT_USER_RENTAL_EXISTS, FORBIDDEN);
                }

                User reportee = report.getReportee();
                reportee = reportee.updateIsRestricted();
                userRepository.save(reportee);
            }

            // 3. 옷장 점수 차감
            case DOCKED -> {
                User reportee = report.getReportee();
                reportee = reportee.updateClosetScore(-2);
                userRepository.save(reportee);
            }

            // 4. 무시
            case IGNORED -> {}
        }

        // 신고 조치 내역 변경
        report = report.updateAction(action);
        reportRepository.save(report);

        return SUCCESS;
    }

    /* 회원 목록 조회 */
    public List<UserListResponse> getUserList(String search) throws BaseException {

        // 유저 목록 불러오기
        List<User> users;
        if (search != null && !search.isEmpty()) {
            // 이름, 닉네임, 이메일 기준으로 대소문자 구분 없이 검색
            users = userRepository.searchAll(search);
        }
        else {
            users = userRepository.findAllByDeletedAtNullOrderByCreatedAtDesc();
        }

        List<UserListResponse> responses = new ArrayList<>();
        for (User user : users) {
            Long userId = user.getId();

            // 거래 후기 키워드 개수
            List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId);
            int positiveKeywordCount = 0;
            int negativeKeywordCount = 0;
            for (Review review : reviews) {
                Long reviewId = review.getId();
                positiveKeywordCount += reviewKeywordRepository.countPositiveKeywordsByReviewId(reviewId);
                negativeKeywordCount += reviewKeywordRepository.countNegativeKeywordsByReviewId(reviewId);
            }

            // 거래 건수
            int rentalCount = rentalInfoRepository.countByBuyerIdOrLenderId(userId, userId);

            responses.add(new UserListResponse(user, positiveKeywordCount, negativeKeywordCount, rentalCount));
        }

        return responses;
    }
}
