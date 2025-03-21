package com.yooyoung.clotheser.rental.dto.response;

import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.global.entity.Time;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class NoRentalClothesListResponse {

    @Schema(title = "보유 옷 id", example = "1")
    private Long id;

    @Schema(title = "보유 옷 썸네일 (첫 번째 사진)", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/clothes/340182f1-8340-444b-bc2f-3149068a117e_%ED%8A%B8%EB%A0%8C%EC%B9%98%EC%BD%94%ED%8A%B8.png")
    private String imgUrl;

    @Schema(title = "상품명", example = "여름 나시")
    private String name;

    @Schema(title = "구매 가격", example = "19900")
    private Integer price;

    @Schema(title = "브랜드", example = "에이블리")
    private String brand;

    @Schema(title = "보유 옷 등록 시간", example = "2시간 전")
    private String createdAt;

    public NoRentalClothesListResponse(Clothes clothes, String imgUrl) {
        this.id = clothes.getId();
        this.imgUrl = imgUrl;
        this.name = clothes.getName();
        this.price = clothes.getPrice();
        this.brand = clothes.getBrand();
        this.createdAt = Time.calculateTime(clothes.getCreatedAt());
    }

}
