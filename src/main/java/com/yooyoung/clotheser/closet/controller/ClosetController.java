package com.yooyoung.clotheser.closet.controller;

import com.yooyoung.clotheser.closet.dto.*;
import com.yooyoung.clotheser.closet.service.ClosetService;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/closet")
@Tag(name = "Closet", description = "나의 옷장 API")
public class ClosetController {

    private final ClosetService closetService;

    @Operation(summary = "나의 보유 옷 목록 조회", description = "'보유 - 옷장' 탭에서 나의 보유 옷 목록 전체를 조회한다.")
    @GetMapping("/clothes")
    public ResponseEntity<BaseResponse<List<UserClothesListResponse>>> getMyClothes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(closetService.getMyClothes(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "남의 보유 옷 목록 조회", description = "'보유 - 옷장' 탭에서 남의 보유 옷 목록 전체를 조회한다.")
    @Parameter(name = "userSid", description = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", required = true)
    @GetMapping("/{userSid}/clothes")
    public ResponseEntity<BaseResponse<List<UserClothesListResponse>>> getUserClothes(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                     @PathVariable String userSid){
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(closetService.getUserClothes(user, userSid)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "나의 대여글 목록 조회", description = "'보유 - 공유 등록' 탭에서 나의 대여글 목록 전체를 조회한다.")
    @GetMapping("/rentals")
    public ResponseEntity<BaseResponse<List<UserRentalListResponse>>> getMyRentals(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(closetService.getMyRentals(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "남의 대여글 목록 조회", description = "'보유 - 공유 등록' 탭에서 남의 대여글 목록 전체를 조회한다.")
    @Parameter(name = "userSid", description = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", required = true)
    @GetMapping("/{userSid}/rentals")
    public ResponseEntity<BaseResponse<List<UserRentalListResponse>>> getUserRentals(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                     @PathVariable String userSid){
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(closetService.getUserRentals(user, userSid)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "공유 내역 조회", description = "'거래 현황 - 공유' 탭에서 대여 중, 대여 완료한 나의 대여글 목록을 조회한다.")
    @GetMapping("/share-history")
    public ResponseEntity<BaseResponse<List<RentalHistoryResponse>>> getShareHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(closetService.getShareHistory(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "대여 내역 조회", description = "'거래 현황 - 대여' 탭에서 대여 중, 대여 완료한 남의 대여글 목록을 조회한다.")
    @GetMapping("/rental-history")
    public ResponseEntity<BaseResponse<List<RentalHistoryResponse>>> getRentalHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(closetService.getRentalHistory(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "나의 보유 옷 찜 목록 조회", description = "'찜 - 옷장' 탭에서 내가 찜한 보유 옷 목록을 조회한다.")
    @GetMapping("/like-clothes")
    public ResponseEntity<BaseResponse<List<LikeClothesListResponse>>> getMyLikeClotheList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(closetService.getMyLikeClothesList(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "나의 대여글 찜 목록 조회", description = "'찜 - 대여' 탭에서 내가 찜한 대여글 목록을 조회한다.")
    @GetMapping("/like-rentals")
    public ResponseEntity<BaseResponse<List<LikeRentalListResponse>>> getMyLikeRentals(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(closetService.getMyLikeRentals(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
