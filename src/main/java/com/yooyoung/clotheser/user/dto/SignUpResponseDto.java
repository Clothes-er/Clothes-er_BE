package com.yooyoung.clotheser.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.User;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class SignUpResponseDto {

    private String name;
    private String nickname;
    private String email;
    private String password;
    private LocalDate birthday;
    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public SignUpResponseDto(User user) {
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.birthday = user.getBirthday();
        this.phoneNumber = user.getPhoneNumber();
        this.createdAt = user.getCreatedAt();
    }

}
