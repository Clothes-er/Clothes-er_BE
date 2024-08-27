package com.yooyoung.clotheser.admin.controller;

import com.yooyoung.clotheser.admin.dto.response.AdminLoginResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportListResponse;
import com.yooyoung.clotheser.admin.dto.response.ReportResponse;
import com.yooyoung.clotheser.admin.service.AdminService;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
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
    public ResponseEntity<BaseResponse<ReportResponse>> getReport(@PathVariable("reportId") Long reportId) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(adminService.getReport(reportId)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
