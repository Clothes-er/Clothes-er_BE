package com.yooyoung.clotheser.review.service;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import com.yooyoung.clotheser.rental.repository.RentalInfoRepository;
import com.yooyoung.clotheser.review.domain.Keyword;
import com.yooyoung.clotheser.review.domain.Review;
import com.yooyoung.clotheser.review.domain.ReviewKeyword;
import com.yooyoung.clotheser.review.dto.ReviewRequest;
import com.yooyoung.clotheser.review.repository.ReviewKeywordRepository;
import com.yooyoung.clotheser.review.repository.ReviewRepository;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.NOT_FOUND_RENTAL_INFO;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ChatRoomRepository chatRoomRepository;
    private final RentalInfoRepository rentalInfoRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;

    // 거래 후기 생성
    public BaseResponseStatus createReview(Long roomId, ReviewRequest reviewRequest, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CHAT_ROOM, NOT_FOUND));

        // 채팅방 참여자인지 확인
        if (!chatRoom.getBuyer().getId().equals(user.getId()) && !chatRoom.getLender().getId().equals(user.getId()) ) {
            throw new BaseException(FORBIDDEN_ENTER_CHAT_ROOM, FORBIDDEN);
        }

        // 대여 완료 상태인 대여 정보 불러오기
        RentalInfo rentalInfo = rentalInfoRepository.findFirstByBuyerIdAndLenderIdAndRentalIdAndState(
                chatRoom.getBuyer().getId(),
                chatRoom.getLender().getId(),
                chatRoom.getRental().getId(),
                RentalState.RETURNED
        ).orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL_INFO, NOT_FOUND));

        // 기존에 후기를 작성했었는지 확인
        if (reviewRepository.existsByRentalInfoIdAndReviewerId(rentalInfo.getId(), user.getId())) {
            throw new BaseException(REVIEW_EXISTS, CONFLICT);
        }

        // 작성한 후기 저장
        User reviewee = rentalInfo.getBuyer().getId().equals(user.getId()) ? rentalInfo.getLender() : rentalInfo.getBuyer();

        Review review = reviewRequest.toReviewEntity(rentalInfo, user, reviewee);
        reviewRepository.save(review);

        List<Keyword> keywords = reviewRequest.getKeywords();
        for (Keyword keyword : keywords) {
            ReviewKeyword reviewKeyword = reviewRequest.toReviewKeywordEntity(review, keyword);
            reviewKeywordRepository.save(reviewKeyword);
        }

        // 상대방의 옷장 점수 반영
        updateClosetScore(reviewee);
        userRepository.save(reviewee);

        return SUCCESS;
    }

    // 누적 후기 건수가 3건일 때마다, 옷장 점수 변경
    public void updateClosetScore(User user) {

        int reviewCount = reviewRepository.countByRevieweeId(user.getId());

        if (reviewCount % 3 == 0 && reviewCount > 0) {
            // 최근 3건 가져와서 평균 구하기
            List<Long> reviewIds = reviewRepository.findTop3ByRevieweeIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(Review::getId)
                    .toList();
            double difference = 0;
            for (Long reviewId : reviewIds) {
                // 키워드별 점수 합치기
                List<Keyword> keywords = reviewKeywordRepository.findAllByReviewId(reviewId)
                        .stream()
                        .map(ReviewKeyword::getKeyword)
                        .toList();
                difference += keywords.stream().mapToDouble(Keyword::getScore).sum();
            }
            difference = difference / 3 * 0.1;
            user.updateClosetScore(difference);
        }
    }

}
