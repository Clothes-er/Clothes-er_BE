package com.yooyoung.clotheser.chat.dto;

import com.yooyoung.clotheser.chat.domain.ChatMessage;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class ChatMessageRequest {

    @Schema(title = "채팅 메시지", description = "255자 이내", example = "안녕하세요~", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    public ChatMessage toEntity(User user, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .message(message)
                .user(user)
                .room(chatRoom)
                .build();
    }

}
