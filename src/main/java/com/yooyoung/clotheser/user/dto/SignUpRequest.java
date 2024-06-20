package com.yooyoung.clotheser.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SignUpRequest {

    @Schema(title = "이름", description = "2 ~ 10자", example = "김눈송", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 2, max = 10, message = "이름은 2 ~ 10자로 입력해주세요.")
    private String name;

    @Schema(title = "닉네임", description = "2 ~ 10자", example = "닉네임", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 10, message = "닉네임은 2 ~ 10자로 입력해주세요.")
    private String nickname;

    @Schema(title = "이메일", description = "255자 이내", example = "noonsong@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(regexp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$",
            message = "이메일 형식이 맞지 않습니다.")
    private String email;

    @Schema(title = "비밀번호", description = "영문, 숫자, 특수문자 포함 8 ~ 15자", example = "noonsong123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,15}$",
            message = " 비밀번호는 영문, 숫자, 특수문자 포함 8 ~ 15자로 입력해주세요.")
    private String password;

    @Schema(title = "비밀번호 확인", description = "password와 동일", example = "noonsong123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 한 번 더 입력해주세요.")
    private String confirmedPassword;

    @Schema(title = "생일", description = "YYYY-MM-DD 형식", example = "2000-02-16", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "생일을 입력해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate birthday;

    @Schema(title = "전화번호", description = "00(0)-000(0)-0000 형식", example = "010-1620-6925", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호는 -로 구분해주세요.")
    private String phoneNumber;

    public User toEntity(String password) {
        return User.builder()
                .name(name)
                .nickname(nickname)
                .email(email)
                .password(password)
                .birthday(birthday)
                .phoneNumber(phoneNumber)
                .build();
    }

}
