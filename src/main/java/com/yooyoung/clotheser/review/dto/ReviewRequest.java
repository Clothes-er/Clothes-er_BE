package com.yooyoung.clotheser.review.dto;

import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.review.domain.Keyword;
import com.yooyoung.clotheser.review.domain.Review;
import com.yooyoung.clotheser.review.domain.ReviewKeyword;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class ReviewRequest {

    @Schema(title = "키워드 후기 리스트", type = "array", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "키워드를 선택해주세요.")
    private List<Keyword> keywords;

    @Schema(title = "텍스트 후기", description = "500자 이내", example = "옷 상태도 좋고 저렴한 가격으로 새로운 옷을 입어볼 수 있었어요! 감사합니다 :)")
    private String content;

    public Review toReviewEntity(RentalInfo rentalInfo, User reviewer, User reviewee) {
        return Review.builder()
                .rentalInfo(rentalInfo)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .isPositive(this.keywords.get(0).getIsPositive())
                .content(this.content)
                .build();
    }

    public ReviewKeyword toReviewKeywordEntity(Review review, Keyword keyword) {
        return ReviewKeyword.builder()
                .review(review)
                .keyword(keyword)
                .build();
    }

}
