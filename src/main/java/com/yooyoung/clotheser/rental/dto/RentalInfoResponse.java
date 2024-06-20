package com.yooyoung.clotheser.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class RentalInfoResponse {

    @Schema(title = "대여 상태", example = "RENTED")
    private RentalState rentalState;

    @Schema(title = "대여 시작일", example = "2024년 06월 20일")
    private LocalDate startDate;
    @Schema(title = "반납 예정일", example = "2024년 06월 30일")
    private LocalDate endDate;

    @Schema(title = "대여 시간", description = "대여중으로 변경된 시점", example = "2024년 06월 20일 17:45:57")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime rentalTime;

    @Schema(title = "반납 시간", description = "대여 완료로 변경된 시점", example = "2024년 06월 30일 11:20:27")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime returnTime;

    public RentalInfoResponse(RentalInfo rentalInfo) {
        this.rentalState = rentalInfo.getState();
        this.startDate = rentalInfo.getStartDate();
        this.endDate = rentalInfo.getEndDate();
        this.rentalTime = rentalInfo.getRentalTime();
        this.returnTime = rentalInfo.getReturnTime();
    }

}
