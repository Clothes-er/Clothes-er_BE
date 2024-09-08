package com.yooyoung.clotheser.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class LogoutRequest {

    @Schema(title = "리프레시 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE3MjU3ODgyMzUsImV4cCI6MTcyNjk5NzgzNX0.tnKzcNkmC9-tuugoNZ90l2w4vR57RLAh3mPedp8FnwF17CJbARJzbLrAS05FPUcWF4LXsEreZjk3B8MdQLu-Sw")
    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    private String refreshToken;

}
