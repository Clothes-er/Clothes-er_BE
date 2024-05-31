package com.yooyoung.clotheser.chat.dto;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.user.domain.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
/* 채팅방 목록 */
public class ChatRoomListResponse {

    private Long id;
    private String recentMessage;

    // 상대방 정보
    private String nickname;
    private String profileImgUrl;

    // 대여글 정보
    private String rentalImgUrl;
    private String title;

    public ChatRoomListResponse(ChatRoom chatRoom, String recentMessage, String rentalImgUrl, User opponent) {
        this.id = chatRoom.getId();
        this.recentMessage = recentMessage;

        this.nickname = opponent.getNickname();
        this.profileImgUrl = opponent.getProfileUrl();

        this.rentalImgUrl = rentalImgUrl;
        this.title = chatRoom.getRental().getTitle();
    }

}
