package com.yooyoung.clotheser.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class AddressResponse {

    @Schema(title = "이메일", example = "noonsong@gmail.com")
    private String email;

    @Schema(title = "위도", example = "37.602354")
    private double latitude;

    @Schema(title = "경도", example = "127.026905")
    private double longitude;

    @Schema(title = "회원 정보 수정한 시간", example = "2024년 06월 20일 19:13:36")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public AddressResponse(User user) {
        this.email = user.getEmail();
        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();
        this.updatedAt = user.getUpdatedAt();
    }

}
