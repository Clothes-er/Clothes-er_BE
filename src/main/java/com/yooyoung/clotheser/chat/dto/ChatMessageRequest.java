package com.yooyoung.clotheser.chat.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class ChatMessageRequest {

    private Long userId;
    private String message;

}
