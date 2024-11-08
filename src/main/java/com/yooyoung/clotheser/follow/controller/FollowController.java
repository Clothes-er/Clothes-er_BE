package com.yooyoung.clotheser.follow.controller;

import com.yooyoung.clotheser.follow.dto.FollowListResponse;
import com.yooyoung.clotheser.follow.service.FollowService;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follows")
@Tag(name = "Follow", description = "팔로우 API")
public class FollowController {
    private final FollowService followService;

    @Operation(summary = "팔로우 생성", description = "특정 유저를 팔로우한다.")
    @Parameter(name = "userSid", description = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", required = true)
    @PostMapping("/{userSid}")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> createFollowing(@PathVariable("userSid") String userSid,
                                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(followService.createFollowing(user, userSid)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "팔로우 삭제", description = "특정 유저 팔로우를 취소한다.")
    @Parameter(name = "userSid", description = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", required = true)
    @DeleteMapping("/{userSid}")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> deleteFollowing(@PathVariable("userSid") String userSid,
                                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(followService.deleteFollowing(user, userSid)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "나의 팔로워 목록 조회", description = "나의 팔로워 목록을 조회한다.")
    @GetMapping("/followers")
    public ResponseEntity<BaseResponse<List<FollowListResponse>>> getMyFollowers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(followService.getMyFollowers(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }
}
