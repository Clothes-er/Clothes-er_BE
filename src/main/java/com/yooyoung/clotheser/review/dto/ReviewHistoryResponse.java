package com.yooyoung.clotheser.review.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class ReviewHistoryResponse {

    private List<KeywordReviewResponse> keywordReviews;
    private List<TextReviewResponse> textReviews;

    public ReviewHistoryResponse(List<KeywordReviewResponse> keywordReviews, List<TextReviewResponse> textReviews) {
        this.keywordReviews = keywordReviews;
        this.textReviews = textReviews;
    }

}
