package com.yooyoung.clotheser.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class RentalInfoRequest {

    @NotNull(message = "대여 시작일을 입력해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;

    @NotNull(message = "반납 예정일을 입력해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate;

    public RentalInfo toEntity(ChatRoom chatRoom) {
        return RentalInfo.builder()
                .startDate(startDate)
                .endDate(endDate)
                .buyer(chatRoom.getBuyer())
                .lender(chatRoom.getLender())
                .rental(chatRoom.getRental())
                .state(RentalState.RENTED)
                .build();
    }

}
