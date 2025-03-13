package com.yooyoung.clotheser.review.dto;

import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class ReviewHistoryResponse {

    @Schema(title = "닉네임", example = "숙명이")
    private String nickname;

    @Schema(title = "프로필 사진", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    private List<KeywordReviewResponse> keywordReviews;
    private List<TextReviewResponse> textReviews;

    public ReviewHistoryResponse(User user, List<KeywordReviewResponse> keywordReviews, List<TextReviewResponse> textReviews) {
        this.nickname = user.getNickname();
        this.profileUrl = user.getProfileUrl();
        this.keywordReviews = keywordReviews;
        this.textReviews = textReviews;
    }

}
