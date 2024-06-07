package com.yooyoung.clotheser.global.util;

import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 총 파일 크기 50MB 제한
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse<BaseResponseStatus>> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return new ResponseEntity<>(new BaseResponse<>(FILE_TOO_LARGE), PAYLOAD_TOO_LARGE);
    }

}
