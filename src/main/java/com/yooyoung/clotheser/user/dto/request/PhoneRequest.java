package com.yooyoung.clotheser.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class PhoneRequest {

    @Schema(title = "전화번호", description = "00(0)-000(0)-0000 형식", example = "010-1620-6925", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호는 -로 구분해주세요.")
    private String phoneNumber;

}
