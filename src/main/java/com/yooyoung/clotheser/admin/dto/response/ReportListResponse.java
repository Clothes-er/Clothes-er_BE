package com.yooyoung.clotheser.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.admin.domain.Report;
import com.yooyoung.clotheser.admin.domain.ReportAction;
import com.yooyoung.clotheser.admin.domain.ReportState;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class ReportListResponse {

    @Schema(title = "신고 id", example = "1")
    private Long id;

    @Schema(title = "신고 대상 닉네임", example = "진도리")
    private String reporteeNickname;

    @Schema(title = "신고 사유", example = "광고")
    private String reason;

    @Schema(title = "신고 내용", example = "광고성 게시물을 올렸어요. 확인 부탁드립니다.")
    private String content;

    @Schema(title = "신고 대상 옷장 점수", example = "10")
    private double closetScore;

    @Schema(title = "처리 상태", example = "PENDING")
    private ReportState state;

    @Schema(title = "신고 조치 내역", example = "RESTRICTED")
    private ReportAction action;

    @Schema(title = "신고한 시간", example = "2024년 06월 20일 17:55:40")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public ReportListResponse(Report report) {
        this.id = report.getId();
        this.reporteeNickname = report.getReportee().getNickname();
        this.reason = report.getReason();
        this.content = report.getContent();

        double closetScore = report.getReportee().getClosetScore();
        if (closetScore == (int) closetScore) {
            this.closetScore = Double.parseDouble(String.format("%d", (int) closetScore));
        }
        else {
            BigDecimal bd = new BigDecimal(closetScore).setScale(1, RoundingMode.DOWN);
            this.closetScore = bd.doubleValue();
        }

        this.state = report.getState();
        this.createdAt = report.getCreatedAt();
    }

}
