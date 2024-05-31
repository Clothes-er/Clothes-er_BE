package com.yooyoung.clotheser.chat.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
// 채팅 메시지
public class ChatMessageResponse {

    private String nickname;
    private String message;
    private LocalDateTime createdAt;

}
