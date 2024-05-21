package com.yooyoung.clotheser.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.rental.domain.Rental;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class RentalListReponse {

    private Long id;
    private String imgUrl;
    private String nickname;
    private String title;

    private int minPrice;

    // TODO: 현재 시간을 기준으로 몇 분/시간/일 전으로 변경 예정
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public RentalListReponse(Rental rental, String imgUrl, int minPrice) {
        this.id = rental.getId();
        this.imgUrl = imgUrl;
        this.nickname = rental.getUser().getNickname();
        this.title = rental.getTitle();
        this.minPrice = minPrice;
        this.createdAt = rental.getCreatedAt();
    }

}
