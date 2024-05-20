package com.yooyoung.clotheser.rental.controller;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.rental.dto.PostRentalRequest;
import com.yooyoung.clotheser.rental.dto.RentalResponse;
import com.yooyoung.clotheser.rental.service.RentalService;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rentals")
public class RentalController {

    private final RentalService rentalService;

    // 대여글 생성
    @PostMapping("/{clothesId}")
    public ResponseEntity<BaseResponse<RentalResponse>> createRentalPost(@Valid @RequestBody PostRentalRequest postRentalRequest,
                                                                         BindingResult bindingResult,
                                                                         @PathVariable Long clothesId,
                                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 입력 유효성 검사
        if (bindingResult.hasErrors()) {
            List<FieldError> list = bindingResult.getFieldErrors();
            for(FieldError error : list) {
                return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
            }
        }

        // 가격 입력 확인
        /*if (postRentalRequest.getPrices() == null || postRentalRequest.getPrices().isEmpty()) {
            return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, "가격을 입력해주세요."), BAD_REQUEST);
        }*/

        return new ResponseEntity<>(new BaseResponse<>(rentalService.createRentalPost(postRentalRequest, clothesId, userDetails.user)), CREATED);
    }

}
