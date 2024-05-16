package com.yooyoung.clotheser.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter(AccessLevel.NONE)
public class LoginResponse {

    private String email;
    private Boolean isFirstLogin;
    private TokenResponse token;

}
