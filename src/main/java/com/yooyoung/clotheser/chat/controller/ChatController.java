package com.yooyoung.clotheser.chat.controller;

import com.yooyoung.clotheser.chat.dto.ChatRoomListResponse;
import com.yooyoung.clotheser.chat.dto.ChatRoomResponse;
import com.yooyoung.clotheser.chat.service.ChatService;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

    /* 채팅방 생성 */
    @PostMapping("/rooms/{rentalId}")
    public ResponseEntity<BaseResponse<ChatRoomResponse>> createChatRoom(@PathVariable("rentalId") Long rentalId,
                                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(chatService.createChatRoom(rentalId, userDetails.user)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 채팅방 목록 조회 */
    @GetMapping("/rooms")
    public ResponseEntity<BaseResponse<List<ChatRoomListResponse>>> getChatRoomList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(chatService.getChatRoomList(userDetails.user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    // TODO: 채팅방 조회 시 buyer, lender 닉네임 보여주기
    /* 채팅방 조회 (채팅 메시지 목록 포함) */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<BaseResponse<ChatRoomResponse>> getChatMessageList(@PathVariable("roomId") Long roomId,
                                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(chatService.getChatRoom(roomId, userDetails.user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
