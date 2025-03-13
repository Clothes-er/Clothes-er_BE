package com.yooyoung.clotheser.global.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ChatRoomException extends Exception {
    private BaseResponseStatus status;
    private HttpStatus httpStatus;
    private Long roomId;
}
