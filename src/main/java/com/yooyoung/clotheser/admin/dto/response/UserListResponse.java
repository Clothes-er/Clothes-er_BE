package com.yooyoung.clotheser.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class UserListResponse {

    @Schema(title = "이름", example = "박숙명")
    private String name;

    @Schema(title = "닉네임", example = "숙명이")
    private String nickname;

    @Schema(title = "이메일", example = "songee@naver.com")
    private String email;

    @Schema(title = "전화번호", example = "010-1234-1234")
    private String phoneNumber;

    @Schema(title = "옷장 점수", example = "10")
    private double closetScore;

    @Schema(title = "후기 긍정 키워드 개수", example = "8")
    private int positiveKeywordCount;

    @Schema(title = "후기 부정 키워드 개수", example = "4")
    private int negativeKeywordCount;

    @Schema(title = "거래 건수", example = "4")
    private int rentalCount;

    @Schema(title = "유예 여부", example = "false")
    private Boolean isSuspended;

    @Schema(title = "이용 제한 여부", example = "false")
    private Boolean isRestricted;

    @Schema(title = "회원가입한 시간", example = "2024년 06월 20일 19:13:36")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public UserListResponse(User user, int positiveKeywordCount, int negativeKeywordCount, int rentalCount) {
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();

        double closetScore = user.getClosetScore();
        if (closetScore == (int) closetScore) {
            this.closetScore = Double.parseDouble(String.format("%d", (int) closetScore));
        }
        else {
            BigDecimal bd = new BigDecimal(closetScore).setScale(1, RoundingMode.DOWN);
            this.closetScore = bd.doubleValue();
        }

        this.positiveKeywordCount = positiveKeywordCount;
        this.negativeKeywordCount = negativeKeywordCount;
        this.rentalCount = rentalCount;
        this.isSuspended = user.getIsSuspended();
        this.isRestricted = user.getIsRestricted();
        this.createdAt = user.getCreatedAt();
    }

}
