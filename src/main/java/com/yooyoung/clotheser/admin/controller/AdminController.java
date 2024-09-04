package com.yooyoung.clotheser.admin.controller;

import com.yooyoung.clotheser.admin.dto.request.ReportActionRequest;
import com.yooyoung.clotheser.admin.dto.response.AdminLoginResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportListResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportResponse;
import com.yooyoung.clotheser.admin.dto.response.UserListResponse;
import com.yooyoung.clotheser.admin.service.AdminService;
import com.yooyoung.clotheser.chat.dto.RentalChatRoomListResponse;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.user.dto.request.LoginRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.REQUEST_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "관리자 API")
public class AdminController {

    private final AdminService adminService;

    /* 관리자 로그인 */
    @Operation(summary = "관리자 로그인", description = "관리자가 로그인을 하여 JWT를 얻는다.")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AdminLoginResponse>> adminLogin(@Valid @RequestBody LoginRequest loginRequest,
                                                                  BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(adminService.adminLogin(loginRequest)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 신고 목록 조회 */
    @Operation(summary = "신고 목록 조회", description = "신고 목록을 조회한다.")
    @GetMapping("/reports")
    public ResponseEntity<BaseResponse<List<ReportListResponse>>> getReportList() {
        try {
            return new ResponseEntity<>(new BaseResponse<>(adminService.getReportList()), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 신고 조회 */
    @Operation(summary = "신고 조회", description = "특정 신고 내역을 조회한다.")
    @Parameter(name = "reportId", description = "신고 id", example = "1", required = true)
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<BaseResponse<ReportResponse>> getReport(@PathVariable Long reportId) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(adminService.getReport(reportId)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 신고 조치 */
    @Operation(summary = "신고 조치", description = "특정 신고에 대한 조치를 취한다.")
    @Parameter(name = "reportId", description = "신고 id", example = "1", required = true)
    @PostMapping("/reports/{reportId}")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> actionReport(@PathVariable Long reportId,
                                                                         @Valid @RequestBody ReportActionRequest reportActionRequest,
                                                                         BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(adminService.actionReport(reportId, reportActionRequest)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 회원 목록 조회 */
    @Operation(summary = "회원 목록 조회", description = "회원 목록을 조회한다.")
    @Parameter(name = "search", description = "이름, 닉네임, 이메일을 기준으로 대소문자 구분 없이 검색한다.", example = "유진")
    @GetMapping("/users")
    public ResponseEntity<BaseResponse<List<UserListResponse>>> getUserList(@RequestParam(required = false) String search) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(adminService.getUserList(search)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 거래 중인 채팅방 목록 조회 */
    @Operation(summary = "거래 중인 채팅방 목록 조회", description = "신고 당한 회원의 거래 중인 채팅방 목록을 조회한다.")
    @Parameter(name = "userSid", description = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", required = true)
    @GetMapping("/chats/{userSid}/rented-rooms")
    public ResponseEntity<BaseResponse<List<RentalChatRoomListResponse>>> getRentedChatRoomList(@PathVariable String userSid) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(adminService.getRentedChatRoomList(userSid)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
