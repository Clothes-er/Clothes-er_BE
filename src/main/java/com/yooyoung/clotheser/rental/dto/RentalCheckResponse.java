package com.yooyoung.clotheser.rental.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class RentalCheckResponse {

    private Long roomId;
    private Boolean isChecked;
    private List<String> checkList;

    public RentalCheckResponse(Long roomId, List<String> checkList) {
        this.roomId = roomId;
        this.isChecked = !(checkList == null || checkList.isEmpty());
        this.checkList = checkList;
    }

}
