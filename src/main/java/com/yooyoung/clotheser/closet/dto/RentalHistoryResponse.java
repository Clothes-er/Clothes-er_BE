package com.yooyoung.clotheser.closet.dto;

import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter(AccessLevel.NONE)
public class RentalHistoryResponse {

    @Schema(title = "대여글 id", example = "3")
    private Long id;

    @Schema(title = "채팅방 id", example = "1")
    private Long roomId;

    @Schema(title = "암호화된 회원 id", example = "xfweriok12")
    private String userSid;

    @Schema(title = "대여글 썸네일 (첫 번째 사진)", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/rentals/baddfbcb-bb3f-40cf-9cfb-af6276c118b9_short_neat_2.png")
    private String imgUrl;

    @Schema(title = "상대방 닉네임", example = "김눈송")
    private String nickname;

    @Schema(title = "상대방 탈퇴 여부", example = "false")
    private Boolean isWithdrawn;

    @Schema(title = "제목", example = "여름 나시")
    private String title;

    @Schema(title = "최소 가격", example = "2000")
    private int minPrice;

    @Schema(title = "최소 일수", example = "3")
    private int minDays;

    @Schema(title = "대여글 삭제 여부", example = "false")
    private Boolean isDeleted;

    @Schema(title = "대여 상태", example = "RENTED")
    private RentalState rentalState;

    @Schema(title = "대여 시작일", example = "2024년 06월 20일")
    private LocalDate startDate;

    @Schema(title = "반납 예정일", example = "2024년 06월 30일")
    private LocalDate endDate;

    public RentalHistoryResponse(Rental rental, Long roomId, String userSid, String imgUrl,
                                 User user, int minPrice, int minDays, RentalInfo rentalInfo) {
        this.id = rental.getId();
        this.roomId = roomId;
        this.userSid = userSid;
        this.imgUrl = imgUrl;
        this.nickname = user.getNickname();
        this.isWithdrawn = user.getDeletedAt() != null;
        this.title = rental.getTitle();
        this.minPrice = minPrice;
        this.minDays = minDays;
        this.isDeleted = rental.getDeletedAt() != null;
        this.rentalState = rentalInfo.getState();
        this.startDate = rentalInfo.getStartDate();
        this.endDate = rentalInfo.getEndDate();
    }

}
