package com.yooyoung.clotheser.review.dto;

import com.yooyoung.clotheser.global.entity.Time;
import com.yooyoung.clotheser.review.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class TextReviewResponse {

    @Schema(title = "암호화된 작성자 id", example = "xfweriok12")
    private String userSid;

    @Schema(title = "작성자 닉네임", example = "김눈송")
    private String nickname;

    @Schema(title = "작성자 프로필 사진", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    @Schema(title = "텍스트 후기", example = "옷 상태도 좋고 너무 저렴하게 잘 이용했어요! 감사합니다 :)")
    private String content;

    @Schema(title = "후기 남긴 시간", example = "3시간 전")
    private String createdAt;

    public TextReviewResponse(String userSid, Review review) {
        this.userSid = userSid;
        this.nickname = review.getReviewer().getNickname();
        this.profileUrl = review.getReviewer().getProfileUrl();
        this.content = review.getContent();
        this.createdAt = Time.calculateTime(review.getCreatedAt());
    }

}
