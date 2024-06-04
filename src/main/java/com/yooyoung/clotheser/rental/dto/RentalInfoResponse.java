package com.yooyoung.clotheser.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class RentalInfoResponse {

    private RentalState rentalState;

    private LocalDate startDate;
    private LocalDate endDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime rentalTime;
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
