package com.yooyoung.clotheser.rental.dto;

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

    // 회원 정보
    private String profileUrl;
    private String nickname;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RentalResponse(User user, Rental rental, List<RentalPriceDto> prices) {
        this.profileUrl = user.getProfileUrl();
        this.nickname = user.getNickname();

        // TODO: 팔로우 기능
        this.followers = 8;
        this.followees = 5;

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