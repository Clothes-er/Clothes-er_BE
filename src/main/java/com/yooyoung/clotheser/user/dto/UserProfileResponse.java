package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.BodyShape;
import com.yooyoung.clotheser.user.domain.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class UserProfileResponse {

    private String nickname;
    private String profileUrl;

    private int level;
    private int rentalCount;

    private int followers = 0;
    private int followees = 0;

    private Integer height;
    private Integer weight;
    private Integer shoeSize;

    private List<String> bodyShapes;
    private List<String> categories;
    private List<String> styles;

    public UserProfileResponse(User user, List<String> bodyShapes, List<String> categories, List<String> styles) {
        this.nickname = user.getNickname();
        this.profileUrl = user.getProfileUrl();

        this.level = user.getUserLevel();
        this.rentalCount = user.getRentalCount();

        // TODO: 팔로우 기능 추가
        this.followers = 8;
        this.followees = 5;

        this.height = user.getHeight();
        this.weight = user.getWeight();
        this.shoeSize = user.getShoeSize();

        this.bodyShapes = bodyShapes;
        this.categories = categories;
        this.styles = styles;
    }

}
