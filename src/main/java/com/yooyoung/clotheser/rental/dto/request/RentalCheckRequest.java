package com.yooyoung.clotheser.rental.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class RentalCheckRequest {

    @Schema(title = "옷 상태 체크리스트", description = "255자 이내", type = "array",
            example = "[\"오른쪽 소매에 오염 있음\", \"구김 있음\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "옷 상태를 입력해주세요.")
    private List<String> checkList;

}
