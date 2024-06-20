package com.yooyoung.clotheser.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class RentalInfoRequest {

    @Schema(title = "대여 시작일", description = "YYYY-MM-DD 형식", example = "2024-06-20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "대여 시작일을 입력해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;

    @Schema(title = "반납 예정일", description = "YYYY-MM-DD 형식", example = "2024-06-25", requiredMode = Schema.RequiredMode.REQUIRED)
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
