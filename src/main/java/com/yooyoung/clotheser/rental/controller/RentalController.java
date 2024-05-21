package com.yooyoung.clotheser.rental.controller;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.rental.dto.PostRentalRequest;
import com.yooyoung.clotheser.rental.dto.RentalListReponse;
import com.yooyoung.clotheser.rental.dto.RentalResponse;
import com.yooyoung.clotheser.rental.service.RentalService;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;

import com.yooyoung.clotheser.user.domain.User;
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
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(rentalService.createRentalPost(postRentalRequest, clothesId, userDetails.user)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    // 대여글 조회
    @GetMapping("/{rentalId}")
    public ResponseEntity<BaseResponse<RentalResponse>> getRental(@PathVariable Long rentalId,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.getRental(rentalId, user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    // TODO: 무한 스크롤을 위한 페이지네이션
    // 대여글 목록 조회 (회원의 주소 기반 반경 2km 이내)
    @GetMapping("")
    public ResponseEntity<BaseResponse<List<RentalListReponse>>> getRentalList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.getRentalList(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }
}
