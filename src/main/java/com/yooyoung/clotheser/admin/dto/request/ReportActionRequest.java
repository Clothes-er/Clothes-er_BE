package com.yooyoung.clotheser.admin.dto.request;

import com.yooyoung.clotheser.admin.domain.ReportAction;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class ReportActionRequest {

    @Schema(title = "신고 조치", example = "RESTRICTED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "신고 조치를 입력해주세요.")
    private ReportAction action;

}
