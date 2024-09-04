package com.yooyoung.clotheser.rental.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.dto.RentalPriceDto;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class RentalResponse {

    @Schema(title = "대여글 id", example = "3")
    private Long id;

    @Schema(title = "보유 옷 id", example = "1")
    private Long clothesId;

    // 회원 정보
    @Schema(title = "암호화된 회원 id", example = "xfweriok12")
    private String userSid;

    @Schema(title = "작성자 프로필 사진 URL", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    @Schema(title = "작성자 닉네임", example = "김눈송")
    private String nickname;

    @Schema(title = "작성자 여부", example = "false")
    private Boolean isWriter;

    @Schema(title = "작성자 유예 여부", example = "false")
    private Boolean isSuspended;

    @Schema(title = "작성자 이용 제한 여부", example = "false")
    private Boolean isRestricted;

    @Schema(title = "대여글 사진 URL 목록", description = "최대 3장", type = "array",
            example = "[\"https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/rentals/0fa7d4e0-de3d-4c87-a814-cc0ee8b8a8fe_black_ops%281%29.jpg\"]")
    private List<String> imgUrls;

    @Schema(title = "제목", example = "여름용 니트")
    private String title;
    @Schema(title = "상세 설명", example = "여름에 입기 너무 좋아요!")
    private String description;

    @Schema(title = "성별", example = "FEMALE")
    private Gender gender;
    @Schema(title = "카테고리", example = "니트")
    private String category;
    @Schema(title = "스타일", example = "캐주얼")
    private String style;

    private List<RentalPriceDto> prices;

    @Schema(title = "브랜드", example = "에이블리")
    private String brand;
    @Schema(title = "사이즈", example = "55")
    private String size;
    @Schema(title = "핏", example = "와이드핏")
    private String fit;

    @Schema(title = "대여글 생성 시간", example = "2024년 06월 20일 17:55:40")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @Schema(title = "대여글 수정 시간", description = "수정 안 하면 null", example = "2024년 06월 21일 21:40:51")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public RentalResponse(User user, String userSid, Rental rental, List<String> imgUrls, List<RentalPriceDto> prices) {
        this.id = rental.getId();
        this.clothesId = rental.getClothesId();

        this.userSid = userSid;
        this.profileUrl = rental.getUser().getProfileUrl();
        this.nickname = rental.getUser().getNickname();
        this.isWriter = rental.getUser().getId().equals(user.getId());
        this.isSuspended = rental.getUser().getIsSuspended();
        this.isRestricted = rental.getUser().getIsRestricted();

        this.imgUrls = imgUrls;

        this.title = rental.getTitle();
        this.description = rental.getDescription();

        this.gender = rental.getGender();
        this.category = rental.getCategory();
        this.style = rental.getStyle();

        this.prices = prices;

        this.brand = rental.getBrand();
        this.size = rental.getSize();
        this.fit = rental.getFit();

        this.createdAt = rental.getCreatedAt();
        this.updatedAt = rental.getUpdatedAt();
    }

}
