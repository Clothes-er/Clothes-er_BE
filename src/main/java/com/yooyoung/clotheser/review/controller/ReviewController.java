package com.yooyoung.clotheser.review.controller;

import com.yooyoung.clotheser.review.dto.ReviewHistoryResponse;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.review.dto.ReviewRequest;
import com.yooyoung.clotheser.review.service.ReviewService;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.REQUEST_ERROR;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Tag(name = "Review", description = "거래 후기 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "거래 후기 생성", description = "상대방에 대한 거래 후기를 작성한다.")
    @Parameter(name = "roomId", description = "채팅방 id", example = "1", required = true)
    @PostMapping("/{roomId}")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> createReview(@PathVariable Long roomId,
                                                                         @Valid @RequestBody ReviewRequest reviewRequest,
                                                                         BindingResult bindingResult,
                                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(reviewService.createReview(roomId, reviewRequest, userDetails.user)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "나의 거래 후기 내역 조회", description = "다른 사람들이 남긴 거래 후기들을 조회한다.")
    @GetMapping("")
    public ResponseEntity<BaseResponse<ReviewHistoryResponse>> getMyReviews(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(reviewService.getMyReviews(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "남의 거래 후기 내역 조회", description = "남의 프로필에서 거래 후기들을 조회한다.")
    @Parameter(name = "userSid", description = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", required = true)
    @GetMapping("/{userSid}")
    public ResponseEntity<BaseResponse<ReviewHistoryResponse>> getUserReviews(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                              @PathVariable String userSid) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(reviewService.getUserReviews(user, userSid)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
