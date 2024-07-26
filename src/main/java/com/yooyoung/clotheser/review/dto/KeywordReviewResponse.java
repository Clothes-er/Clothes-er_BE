package com.yooyoung.clotheser.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class KeywordReviewResponse {

    @Schema(title = "받은 키워드", example = "친절해요")
    private String keyword;

    @Schema(title = "받은 키워드 개수", example = "4")
    private int count;

    public KeywordReviewResponse(String keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }

}
