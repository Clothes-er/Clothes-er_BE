package com.yooyoung.clotheser.closet.dto;

import com.yooyoung.clotheser.clothes.dto.ClothesListResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class LikeClothesListResponse {

    private ClothesListResponse clothesListResponse;

    @Schema(title = "찜 여부", example = "true")
    private Boolean isLiked;

    public LikeClothesListResponse(ClothesListResponse clothesListResponse, boolean isLiked) {
        this.clothesListResponse = clothesListResponse;
        this.isLiked = isLiked;
    }
}
