package com.yooyoung.clotheser.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.chat.domain.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
// 채팅 메시지
public class ChatMessageResponse {

    @Schema(title = "전송자 닉네임", example = "김눈송")
    private String nickname;

    @Schema(title = "채팅 메시지", example = "안녕하세요~")
    private String message;

    @Schema(title = "보낸 시간", example = "2024년 06월 20일 16:36:25")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public ChatMessageResponse(ChatMessage chatMessage) {
        this.nickname = chatMessage.getUser().getNickname();
        this.message = chatMessage.getMessage();
        this.createdAt = chatMessage.getCreatedAt();
    }

}
