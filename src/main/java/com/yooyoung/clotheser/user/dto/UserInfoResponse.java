package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter(AccessLevel.NONE)
public class UserInfoResponse {

    private String profileUrl;

    private String name;
    private String nickname;

    private String email;

    private String phoneNumber;
    private LocalDate birthday;

    public UserInfoResponse(User user) {
        this.profileUrl = user.getProfileUrl();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.birthday = user.getBirthday();
    }

}
