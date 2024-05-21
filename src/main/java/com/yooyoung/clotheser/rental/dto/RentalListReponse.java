package com.yooyoung.clotheser.rental.dto;

import com.yooyoung.clotheser.global.entity.Time;
import com.yooyoung.clotheser.rental.domain.Rental;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class RentalListReponse {

    private Long id;
    private String imgUrl;
    private String nickname;
    private String title;

    private int minPrice;

    private String createdAt;

    public RentalListReponse(Rental rental, String imgUrl, int minPrice) {
        this.id = rental.getId();
        this.imgUrl = imgUrl;
        this.nickname = rental.getUser().getNickname();
        this.title = rental.getTitle();
        this.minPrice = minPrice;
        this.createdAt = Time.calculateTime(rental.getCreatedAt());
    }

}
