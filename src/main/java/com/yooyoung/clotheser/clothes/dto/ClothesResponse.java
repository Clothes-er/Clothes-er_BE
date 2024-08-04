package com.yooyoung.clotheser.clothes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.clothes.domain.Clothes;
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
public class ClothesResponse {

    @Schema(title = "보유 옷 id", example = "1")
    private Long id;

    @Schema(title = "대여글 id", example = "1")
    private Long rentalId;

    // 회원 정보
    @Schema(title = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09")
    private String userSid;

    @Schema(title = "작성자 프로필 사진 URL", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    @Schema(title = "작성자 닉네임", example = "김눈송")
    private String nickname;

    @Schema(title = "작성자 여부", example = "false")
    private Boolean isWriter;

    // 보유 옷 정보
    @Schema(title = "보유 옷 사진 URL 목록", description = "최대 3장", type = "array",
            example = "[\"https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/clothess/0fa7d4e0-de3d-4c87-a814-cc0ee8b8a8fe_black_ops%281%29.jpg\"]")
    private List<String> imgUrls;

    @Schema(title = "상품명", example = "스퀘어 아이보리 블라우스")
    private String name;

    @Schema(title = "옷 후기", example = "허리 라인에 밴딩이 있어서 활동성이 좋아요!")
    private String description;

    @Schema(title = "성별", example = "FEMALE")
    private Gender gender;

    @Schema(title = "카테고리", example = "블라우스")
    private String category;

    @Schema(title = "스타일", example = "캐주얼")
    private String style;

    @Schema(title = "구매 가격", example = "19,900")
    private Integer price;

    @Schema(title = "브랜드", example = "에이블리")
    private String brand;

    @Schema(title = "사이즈", example = "FREE")
    private String size;

    @Schema(title = "구매처 링크", example = "https://m.a-bly.com/goods/7995907")
    private String shoppingUrl;

    @Schema(title = "공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(title = "보유 옷 생성 시간", example = "2024년 06월 20일 17:55:40")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @Schema(title = "보유 옷 수정 시간", description = "수정 안 하면 null", example = "2024년 06월 21일 21:40:51")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public ClothesResponse(User user, String userSid, Clothes clothes, List<String> imgUrls) {
        this.id = clothes.getId();
        this.rentalId = clothes.getRentalId();

        this.userSid = userSid;
        this.profileUrl = clothes.getUser().getProfileUrl();
        this.nickname = clothes.getUser().getNickname();
        this.isWriter = clothes.getUser().getId().equals(user.getId());

        this.imgUrls = imgUrls;

        this.name = clothes.getName();
        this.description = clothes.getDescription();

        this.gender = clothes.getGender();
        this.category = clothes.getCategory();
        this.style = clothes.getStyle();

        this.price = clothes.getPrice();
        this.brand = clothes.getBrand();
        this.size = clothes.getSize();
        this.shoppingUrl = clothes.getShoppingUrl();
        this.isPublic = clothes.getIsPublic();

        this.createdAt = clothes.getCreatedAt();
        this.updatedAt = clothes.getUpdatedAt();
    }
    
    
}
