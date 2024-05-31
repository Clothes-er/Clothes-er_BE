package com.yooyoung.clotheser.chat.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
// 채팅방 목록
public class ChatRoomListResponse {

    private Long id;
    private String nickname;
    private String profileImgUrl;
    private String recentMessage;

    private String rentalImgUrl;
    private String title;

}
