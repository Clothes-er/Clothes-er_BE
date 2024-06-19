package com.yooyoung.clotheser.rental.controller;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.rental.dto.*;
import com.yooyoung.clotheser.rental.service.RentalService;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;

import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rentals")
@Tag(name = "Rental", description = "공유 옷장 API")
public class RentalController {

    private final RentalService rentalService;

    @Operation(summary = "대여글 생성", description = "대여글을 생성한다.")
    @PostMapping("")
    public ResponseEntity<BaseResponse<RentalResponse>> createRentalPost(@Valid @RequestPart("post") RentalRequest rentalRequest,
                                                                         BindingResult bindingResult,
                                                                         @RequestPart(value = "images", required = false) MultipartFile[] images,
                                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            // 대여글 이미지 최대 3장
            if (images.length > 3) {
                throw new BaseException(TOO_MANY_IMAGES, PAYLOAD_TOO_LARGE);
            }

            return new ResponseEntity<>(new BaseResponse<>(rentalService.createRentalPost(rentalRequest, images, userDetails.user)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "대여글 조회", description = "대여글의 상세 정보를 조회한다.")
    @Parameter(name = "rentalId", description = "대여글 id", example = "1", required = true)
    @GetMapping("/{rentalId}")
    public ResponseEntity<BaseResponse<RentalResponse>> getRental(@PathVariable Long rentalId,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.getRental(rentalId, user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "대여글 목록 조회", description = "회원의 주소를 기준으로 반경 2km 이내의 대여글 목록을 조회한다.")
    @Parameter(name = "search", description = "제목을 기준으로 검색한다.", example = "블라우스")
    @GetMapping("")
    public ResponseEntity<BaseResponse<List<RentalListReponse>>> getRentalList(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                               @RequestParam(value = "search", required = false) String search) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.getRentalList(user, search)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 옷 상태 체크하기 (대여자만) */
    @Operation(summary = "옷 상태 체크하기", description = "대여자는 옷 상태를 확인하여 체크리스트를 생성한다.")
    @Parameter(name = "roomId", description = "채팅방 id", example = "1", required = true)
    @PostMapping("/{roomId}/check")
    public ResponseEntity<BaseResponse<RentalCheckResponse>> createRentalCheck(@Valid @RequestBody RentalCheckRequest rentalCheckRequest,
                                                                               BindingResult bindingResult,
                                                                               @PathVariable("roomId") Long roomId,
                                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(rentalService.createRentalCheck(rentalCheckRequest, roomId, userDetails.user)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 옷 상태 체크 내역 조회 */
    @Operation(summary = "옷 상태 체크 내역 조회", description = "옷 상태 체크리스트를 조회한다.")
    @Parameter(name = "roomId", description = "채팅방 id", example = "1", required = true)
    @GetMapping("/{roomId}/check")
    public ResponseEntity<BaseResponse<RentalCheckResponse>> getRentalCheck(@PathVariable("roomId") Long roomId,
                                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.getRentalCheck(roomId, user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 대여하기 */
    @Operation(summary = "대여하기", description = "판매자는 대여중 상태로 변경한다.")
    @Parameter(name = "roomId", description = "채팅방 id", example = "1", required = true)
    @PostMapping("/{roomId}/rental")
    public ResponseEntity<BaseResponse<RentalInfoResponse>> createRentalInfo(@Valid @RequestBody RentalInfoRequest rentalInfoRequest,
                                                                             BindingResult bindingResult,
                                                                             @PathVariable("roomId") Long roomId,
                                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(rentalService.createRentalInfo(rentalInfoRequest, roomId, userDetails.user)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 반납하기 */
    @Operation(summary = "반납하기", description = "판매자는 대여 완료 상태로 변경한다.")
    @Parameter(name = "roomId", description = "채팅방 id", example = "1", required = true)
    @PatchMapping("/{roomId}/return")
    public ResponseEntity<BaseResponse<RentalInfoResponse>> updateRentalInfo(@PathVariable("roomId") Long roomId,
                                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.updateRentalInfo(roomId, user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }
}
