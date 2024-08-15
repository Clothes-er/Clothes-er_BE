package com.yooyoung.clotheser.chat.controller;

import com.yooyoung.clotheser.chat.dto.RentalChatRoomListResponse;
import com.yooyoung.clotheser.chat.dto.RentalChatRoomResponse;
import com.yooyoung.clotheser.chat.service.RentalChatService;
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

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/rental-rooms")
@Tag(name = "ChatRoom (Rental)", description = "대여글 채팅방 API")
public class RentalChatController {

    private final RentalChatService rentalChatService;

    @Operation(summary = "대여글 채팅방 생성", description = "대여자는 대여글 채팅방을 생성한다.")
    @Parameter(name = "rentalId", description = "대여글 id", example = "1", required = true)
    @PostMapping("/{rentalId}")
    public ResponseEntity<BaseResponse<RentalChatRoomResponse>> createRentalChatRoom(@PathVariable("rentalId") Long rentalId,
                                                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(rentalChatService.createRentalChatRoom(rentalId, userDetails.user)), CREATED);
        }
        catch (ChatRoomException exception) {
            // 채팅방 이미 존재 시 응답값에 기존 채팅방 id 포함
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus(), exception.getRoomId()), exception.getHttpStatus());
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "대여글 채팅방 목록 조회", description = "대여글 채팅방 목록을 조회한다.")
    @GetMapping("")
    public ResponseEntity<BaseResponse<List<RentalChatRoomListResponse>>> getRentalChatRoomList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(rentalChatService.getRentalChatRoomList(userDetails.user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "대여글 채팅방 조회", description = "대여글 채팅방의 채팅 내역을 조회한다.")
    @Parameter(name = "roomId", description = "채팅방 id", example = "1", required = true)
    @GetMapping("/{roomId}")
    public ResponseEntity<BaseResponse<RentalChatRoomResponse>> getRentalChatRoom(@PathVariable("roomId") Long roomId,
                                                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(rentalChatService.getRentalChatRoom(roomId, userDetails.user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
