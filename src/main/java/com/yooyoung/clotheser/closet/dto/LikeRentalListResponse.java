package com.yooyoung.clotheser.closet.dto;

import com.yooyoung.clotheser.rental.dto.response.RentalListResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class LikeRentalListResponse {

    private RentalListResponse rentalListResponse;

    @Schema(title = "찜 여부", example = "true")
    private Boolean isLiked;

    public LikeRentalListResponse(RentalListResponse rentalListResponse, boolean isLiked) {
        this.rentalListResponse = rentalListResponse;
        this.isLiked = isLiked;
    }
}
