package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.User;
import lombok.*;

import java.time.LocalDate;

@Data
@Setter(AccessLevel.NONE)
public class SignUpResponseDto {

    private String name;
    private String nickname;
    private String email;
    private String password;
    private LocalDate birthday;
    private String phoneNumber;

    public SignUpResponseDto(User user) {
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.birthday = user.getBirthday();
        this.phoneNumber = user.getPhoneNumber();
    }

}
