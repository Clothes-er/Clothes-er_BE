package com.yooyoung.clotheser.closet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class UserLikeRentalListResponse {

    private UserRentalListResponse userRentalListResponse;

    @Schema(title = "찜 여부", example = "true")
    private Boolean isLiked;

    public UserLikeRentalListResponse(UserRentalListResponse userRentalListResponse, boolean isLiked) {
        this.userRentalListResponse = userRentalListResponse;
        this.isLiked = isLiked;
    }
}
