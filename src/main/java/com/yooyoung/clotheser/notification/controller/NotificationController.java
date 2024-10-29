package com.yooyoung.clotheser.notification.controller;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.notification.dto.DeviceTokenRequest;
import com.yooyoung.clotheser.notification.dto.HomeNotificationResponse;
import com.yooyoung.clotheser.notification.service.NotificationService;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.REQUEST_ERROR;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "디바이스 토큰 저장", description = "디바이스 토큰을 저장한다.")
    @PatchMapping("/token")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> saveDeviceToken(@Valid @RequestBody DeviceTokenRequest deviceTokenRequest,
                                                                            BindingResult bindingResult,
                                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(notificationService.saveDeviceToken(
                    userDetails.user, deviceTokenRequest)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "홈 알림 확인 여부 조회", description = "홈 화면에서 알림 확인 여부를 조회한다.")
    @GetMapping("/home")
    public ResponseEntity<BaseResponse<HomeNotificationResponse>> getHomeNotification(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.user;
        return new ResponseEntity<>(new BaseResponse<>(notificationService.getHomeNotification(user)), OK);
    }

    // TODO: 알림 목록 조회

    // TODO: 알림 조회

}
