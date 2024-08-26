package com.yooyoung.clotheser.user.dto.request;

import com.yooyoung.clotheser.admin.domain.Report;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class ReportRequest {

    @Schema(title = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "신고하려는 회원의 암호화된 id를 입력해주세요.")
    private String userSid;

    @Schema(title = "신고 사유", example = "연락 두절", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "신고 사유를 입력해주세요.")
    private String reason;

    @Schema(title = "신고 내용", example = "대여일에 만나기로 했는데 잠수 탔어요.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "신고 사유를 입력해주세요.")
    private String content;

    public Report toEntity(User user, User reportee) {
        return Report.builder()
                .reporter(user)
                .reportee(reportee)
                .reason(reason)
                .content(content)
                .build();
    }
}
