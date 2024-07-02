package com.yooyoung.clotheser.global.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.SUCCESS;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class BaseResponse<T> {

    @Schema(title = "요청 성공 여부", example = "true")
    @JsonProperty("isSuccess")
    private final Boolean isSuccess;

    @Schema(title = "요청에 대한 메시지", example = "요청에 성공하였습니다.")
    private final String message;

    @Schema(title = "자체 서버 코드", example = "2000")
    private final int code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    // 요청에 성공한 경우
    public BaseResponse(T result) {
        this.isSuccess = SUCCESS.isSuccess();
        this.message = SUCCESS.getMessage();
        this.code = SUCCESS.getCode();
        this.result = result;
    }

    // 요청에 실패한 경우 or 성공 시 메시지만 보낼 경우
    public BaseResponse(BaseResponseStatus status) {
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
    }

    // 요청에 실패한 경우 (DTO 입력 검사)
    public BaseResponse(BaseResponseStatus status, String message) {
        this.isSuccess = status.isSuccess();
        this.message = message;
        this.code = status.getCode();       // 2000 고정
    }

    @SuppressWarnings("unchecked")
    // 채팅방 생성 시 이미 존재하는 경우
    public BaseResponse(BaseResponseStatus status, Long roomId) {
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
        this.result = (T) new HashMap<String, Long>() {{
            put("roomId", roomId);
        }};
    }
}