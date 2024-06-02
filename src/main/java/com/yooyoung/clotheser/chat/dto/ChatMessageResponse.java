package com.yooyoung.clotheser.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.chat.domain.ChatMessage;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public ChatMessageResponse(ChatMessage chatMessage) {
        this.nickname = chatMessage.getUser().getNickname();
        this.message = chatMessage.getMessage();
        this.createdAt = chatMessage.getCreatedAt();
    }

}
