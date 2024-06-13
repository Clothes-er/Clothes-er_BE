package com.yooyoung.clotheser.rental.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class RentalCheckRequest {

    @Valid
    @NotEmpty(message = "옷 상태를 입력해주세요.")
    private List<String> checkList;

}
