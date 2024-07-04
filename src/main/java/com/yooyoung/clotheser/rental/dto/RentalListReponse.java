package com.yooyoung.clotheser.rental.dto;

import com.yooyoung.clotheser.global.entity.Time;
import com.yooyoung.clotheser.rental.domain.Rental;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class RentalListReponse {

    @Schema(title = "대여글 id", example = "3")
    private Long id;

    @Schema(title = "암호화된 회원 id", example = "xfweriok12")
    private String userSid;

    @Schema(title = "대여글 썸네일 (첫 번째 사진)", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/rentals/baddfbcb-bb3f-40cf-9cfb-af6276c118b9_short_neat_2.png")
    private String imgUrl;

    @Schema(title = "작성자 닉네임", example = "김눈송")
    private String nickname;

    @Schema(title = "제목", example = "여름 나시")
    private String title;

    @Schema(title = "최소 가격", example = "2000")
    private int minPrice;

    @Schema(title = "대여글 생성 시간", example = "2시간 전")
    private String createdAt;

    public RentalListReponse(Rental rental, String userSid, String imgUrl, int minPrice) {
        this.id = rental.getId();
        this.userSid = userSid;
        this.imgUrl = imgUrl;
        this.nickname = rental.getUser().getNickname();
        this.title = rental.getTitle();
        this.minPrice = minPrice;
        this.createdAt = Time.calculateTime(rental.getCreatedAt());
    }

}
