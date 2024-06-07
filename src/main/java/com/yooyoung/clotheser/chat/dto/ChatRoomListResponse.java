package com.yooyoung.clotheser.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.rental.domain.RentalState;
import com.yooyoung.clotheser.user.domain.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
/* 채팅방 목록 */
public class ChatRoomListResponse {

    private Long id;

    // 상대방 정보
    private String nickname;
    private String profileImgUrl;

    // 대여글 정보
    private String title;
    private String rentalImgUrl;

    // 대여 상태
    private RentalState rentalState;

    // 최근 메시지
    private String recentMessage;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime recentMessageTime;


    public ChatRoomListResponse(ChatRoom chatRoom, RentalState rentalState, String recentMessage, String rentalImgUrl, User opponent) {
        this.id = chatRoom.getId();

        this.nickname = opponent.getNickname();
        this.profileImgUrl = opponent.getProfileUrl();

        this.title = chatRoom.getRental().getTitle();
        this.rentalImgUrl = rentalImgUrl;

        this.rentalState = rentalState;

        this.recentMessage = recentMessage;
        this.recentMessageTime = chatRoom.getUpdatedAt();


    }

}
