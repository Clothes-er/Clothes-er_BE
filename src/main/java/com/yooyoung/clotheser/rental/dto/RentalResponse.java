package com.yooyoung.clotheser.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class RentalResponse {

    private Long id;

    // 회원 정보
    private String profileUrl;
    private String nickname;
    private Boolean isWriter;

    // TODO: 팔로우 기능
    private int followers = 0;
    private int followees = 0;

    private List<String> imgUrls;

    private String title;
    private String description;

    private Gender gender;
    private String category;
    private String style;

    private List<RentalPriceDto> prices;

    private String brand;
    private String size;
    private String fit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public RentalResponse(User user, Rental rental, List<String> imgUrls, List<RentalPriceDto> prices) {
        this.id = rental.getId();

        this.profileUrl = rental.getUser().getProfileUrl();
        this.nickname = rental.getUser().getNickname();
        this.isWriter = rental.getUser().getId().equals(user.getId());

        // TODO: 팔로우 기능
        this.followers = 8;
        this.followees = 5;

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
