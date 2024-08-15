package com.yooyoung.clotheser.chat.controller;

import com.yooyoung.clotheser.chat.dto.UserChatRoomListResponse;
import com.yooyoung.clotheser.chat.dto.UserChatRoomResponse;
import com.yooyoung.clotheser.chat.service.UserChatService;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.ChatRoomException;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/user-rooms")
@Tag(name = "ChatRoom - User", description = "유저 채팅방 API")
public class UserChatController {

    private final UserChatService userChatService;

    @Operation(summary = "유저 채팅방 생성", description = "회원은 유저 채팅방을 생성한다.")
    @Parameter(name = "userSid", description = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", required = true)
    @PostMapping("/{userSid}")
    public ResponseEntity<BaseResponse<UserChatRoomResponse>> createUserChatRoom(@PathVariable String userSid,
                                                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(userChatService.createUserChatRoom(userSid, userDetails.user)), CREATED);
        }
        catch (ChatRoomException exception) {
            // 채팅방 이미 존재 시 응답값에 기존 채팅방 id 포함
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus(), exception.getRoomId()), exception.getHttpStatus());
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "유저 채팅방 목록 조회", description = "유저 채팅방 목록을 조회한다.")
    @GetMapping("")
    public ResponseEntity<BaseResponse<List<UserChatRoomListResponse>>> getUserChatRoomList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(userChatService.getUserChatRoomList(userDetails.user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "유저 채팅방 조회", description = "유저 채팅방의 채팅 내역을 조회한다.")
    @Parameter(name = "roomId", description = "채팅방 id", example = "1", required = true)
    @GetMapping("/{roomId}")
    public ResponseEntity<BaseResponse<UserChatRoomResponse>> getUserChatRoom(@PathVariable("roomId") Long roomId,
                                                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(userChatService.getUserChatRoom(roomId, userDetails.user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
