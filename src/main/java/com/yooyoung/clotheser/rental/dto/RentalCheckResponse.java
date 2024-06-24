package com.yooyoung.clotheser.rental.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class RentalCheckResponse {

    @Schema(title = "채팅방 id", example = "1")
    private Long roomId;

    @Schema(title = "옷 상태 체크 여부", example = "false")
    private Boolean isChecked;

    @Schema(title = "옷 상태 체크리스트", type = "array", example = "[\"오른쪽 소매에 오염 있음\", \"구김 있음\"]")
    private List<String> checkList;

    public RentalCheckResponse(Long roomId, List<String> checkList) {
        this.roomId = roomId;
        this.isChecked = !(checkList == null || checkList.isEmpty());
        this.checkList = checkList;
    }

}
