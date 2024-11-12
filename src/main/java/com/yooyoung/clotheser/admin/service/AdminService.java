package com.yooyoung.clotheser.admin.service;

import com.yooyoung.clotheser.admin.domain.Report;
import com.yooyoung.clotheser.admin.domain.ReportAction;
import com.yooyoung.clotheser.admin.dto.request.ReportActionRequest;
import com.yooyoung.clotheser.admin.dto.response.AdminLoginResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportListResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportResponse;
import com.yooyoung.clotheser.admin.dto.response.UserListResponse;
import com.yooyoung.clotheser.admin.repository.ReportRepository;
import com.yooyoung.clotheser.chat.domain.ChatMessage;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.dto.RentalChatRoomListResponse;
import com.yooyoung.clotheser.chat.repository.ChatMessageRepository;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.jwt.JwtProvider;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.notification.domain.NotificationType;
import com.yooyoung.clotheser.notification.dto.NotificationRequest;
import com.yooyoung.clotheser.notification.service.NotificationService;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.domain.RentalState;
import com.yooyoung.clotheser.rental.repository.RentalImgRepository;
import com.yooyoung.clotheser.rental.repository.RentalInfoRepository;
import com.yooyoung.clotheser.review.domain.Review;
import com.yooyoung.clotheser.review.repository.ReviewKeywordRepository;
import com.yooyoung.clotheser.review.repository.ReviewRepository;
import com.yooyoung.clotheser.user.domain.Role;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.dto.request.LoginRequest;
import com.yooyoung.clotheser.user.dto.response.TokenResponse;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final AESUtil aesUtil;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final NotificationService notificationService;

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    private final ReviewRepository reviewRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;

    private final RentalInfoRepository rentalInfoRepository;
    private final RentalImgRepository rentalImgRepository;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    /* 관리자 로그인 */
    public AdminLoginResponse adminLogin(LoginRequest loginRequest) throws BaseException {
        // 이메일로 회원 존재 확인
        User user = userRepository.findByEmailAndDeletedAtNull(loginRequest.getEmail())
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER_BY_EMAIL, NOT_FOUND));

        // 관리자 확인
        if (user.getIsAdmin() != Role.ADMIN) {
            throw new BaseException(FORBIDDEN_ACCESS_JWT, FORBIDDEN);
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BaseException(LOGIN_MISMATCH, BAD_REQUEST);
        }

        // 토큰 생성
        TokenResponse tokenResponse = jwtProvider.createToken(user.getId(), user.getIsAdmin().name());

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
            String userSid = aesUtil.encryptUserId(reporteeId);

            // 거래중 여부 확인
            boolean isRented = rentalInfoRepository.existsByBuyerIdAndStateOrLenderIdAndState(
                    reporteeId, RentalState.RENTED, reporteeId, RentalState.RENTED
            );

            responses.add(new ReportListResponse(report, userSid, isRented));
        }

        return responses;
    }

    /* 신고 조회 */
    public ReportResponse getReport(Long reportId) throws BaseException {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_REPORT, NOT_FOUND));

        Long reporteeId = report.getReportee().getId();

        // 유저 id 암호화
        String userSid = aesUtil.encryptUserId(reporteeId);

        // 거래중 여부 확인
        boolean isRented = rentalInfoRepository.existsByBuyerIdAndStateOrLenderIdAndState(
                reporteeId, RentalState.RENTED, reporteeId, RentalState.RENTED
        );

        return new ReportResponse(report, userSid, isRented);
    }

    /* 신고 처리 */
    public BaseResponseStatus actionReport(Long reportId, ReportActionRequest reportActionRequest) throws BaseException {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_REPORT, NOT_FOUND));

        ReportAction action = reportActionRequest.getAction();
        User reportee = report.getReportee();
        User reporter = report.getReporter();
        switch (action) {
            case SUSPENDED -> {
                reportee = reportee.updateIsSuspended();
                userRepository.save(reportee);

                String message = "신고가 접수되어 유예 상태가 되었습니다.";
                sendFCMNotification(reportee, message, NotificationType.REPORT);

                // 신고자에게는 최초 신고 처리만 푸시 알림 전송
                if (report.getAction() == null) {
                    message = "신고 내용 검토 결과 " + reportee.getNickname() + " 님이 유예 상태가 되었습니다.";
                    sendFCMNotification(reporter, message, NotificationType.REPORT);
                }
            }
            case RESTRICTED -> {
                boolean isRented = rentalInfoRepository.existsByBuyerIdAndStateOrLenderIdAndState(
                        report.getReportee().getId(), RentalState.RENTED, report.getReportee().getId(), RentalState.RENTED
                );
                if (isRented) {
                    throw new BaseException(REPORT_USER_RENTAL_EXISTS, FORBIDDEN);
                }

                reportee = reportee.updateIsRestricted();
                userRepository.save(reportee);

                String message = "신고가 접수되어 이용이 제한되었습니다.";
                sendFCMNotification(reportee, message, NotificationType.REPORT_NO_URL);
                // TODO: 신고 당한 유저에게 이메일 발송

                // 신고자에게는 최초 신고 처리만 푸시 알림 전송
                if (report.getAction() == null) {
                    message = "신고 내용 검토 결과 " + reportee.getNickname() + " 님이 이용 제한되었습니다.";
                    sendFCMNotification(reporter, message, NotificationType.REPORT);
                }
            }
            case DOCKED -> {
                reportee = reportee.updateClosetScore(-2);
                userRepository.save(reportee);

                String message = "신고가 접수되어 옷장 점수가 차감되었습니다.";
                sendFCMNotification(reportee, message, NotificationType.REPORT);

                // 신고자에게는 최초 신고 처리만 푸시 알림 전송
                if (report.getAction() == null) {
                    message = "신고 내용 검토 결과 " + reportee.getNickname() + " 님의 옷장 점수가 차감되었습니다.";
                    sendFCMNotification(reporter, message, NotificationType.REPORT);
                }
            }
            case IGNORED -> {
                // 신고자에게는 최초 신고 처리만 푸시 알림 전송
                if (report.getAction() == null) {
                    String message = "신고 내용 검토 결과 신고가 반려되었습니다.";
                    sendFCMNotification(reporter, message, NotificationType.REPORT);
                }
            }
        }

        // 신고 조치 내역 변경
        report = report.updateAction(action);
        reportRepository.save(report);

        return SUCCESS;
    }

    private void sendFCMNotification(User user, String message, NotificationType type) throws BaseException {
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .user(user)
                .type(type)
                .image("https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/%EB%A1%9C%EA%B3%A0+256x256.png")
                .title("신고")
                .sourceId("")
                .content(message)
                .build();
        notificationService.sendNotification(notificationRequest);
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

            responses.add(new UserListResponse(user,positiveKeywordCount, negativeKeywordCount, rentalCount));
        }

        return responses;
    }

    /* 거래 중인 채팅방 목록 조회 */
    public List<RentalChatRoomListResponse> getRentedChatRoomList(String userSid) throws BaseException {
        // 조회하려는 회원 불러오기
        Long userId = aesUtil.decryptUserSid(userSid);
        User reportee = userRepository.findByIdAndDeletedAtNull(userId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER, NOT_FOUND));

        // 채팅방 목록 불러오기
        List<ChatRoom> chatRoomList = chatRoomRepository.findRentedChatRoomsByUserId(reportee.getId());
        List<RentalChatRoomListResponse> responses = new ArrayList<>();
        for (ChatRoom chatRoom : chatRoomList) {
            // 로그인한 회원이 대여자인지 판매자인지 구분
            User opponent;
            if (chatRoom.getBuyer().getId().equals(reportee.getId())) {
                opponent = chatRoom.getLender();
            }
            else {
                opponent = chatRoom.getBuyer();
            }

            // 상대방의 id 암호화하기
            String opponentSid = aesUtil.encryptUserId(opponent.getId());

            // 채팅방의 최근 메시지 불러오기
            Optional<ChatMessage> optionalMessage = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(chatRoom.getId());
            String recentMessage = optionalMessage.map(ChatMessage::getMessage).orElse(null);

            // 대여글의 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(chatRoom.getRental().getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            responses.add(new RentalChatRoomListResponse(chatRoom, opponentSid, RentalState.RENTED,
                    recentMessage, imgUrl, opponent));
        }

        return responses;
    }
}
