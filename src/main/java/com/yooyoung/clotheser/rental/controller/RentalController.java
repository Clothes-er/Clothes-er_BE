package com.yooyoung.clotheser.rental.controller;

import com.yooyoung.clotheser.global.entity.*;
import com.yooyoung.clotheser.rental.dto.*;
import com.yooyoung.clotheser.rental.service.RentalService;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;

import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<RentalResponse>> createRental(@Valid @RequestPart("post") RentalRequest rentalRequest,
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

            if (images == null) {
                images = new MultipartFile[0];
            }

            // 대여글 이미지 최대 3장
            if (images.length > 3) {
                throw new BaseException(TOO_MANY_RENTAL_IMAGES, PAYLOAD_TOO_LARGE);
            }

            return new ResponseEntity<>(new BaseResponse<>(rentalService.createRental(rentalRequest, images, userDetails.user)), CREATED);
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
    @Parameters({
            @Parameter(name = "search", description = "제목을 기준으로 대소문자 구분 없이 검색한다.", example = "블라우스"),
            @Parameter(name = "sort", description = "특정 기준으로 정렬한다.", example = "createdAt"),
            @Parameter(name = "gender", description = "성별을 기준으로 필터링한다. (중복 가능)"),
            @Parameter(name = "minHeight", description = "최소 키를 기준으로 필터링한다.", example = "158"),
            @Parameter(name = "maxHeight", description = "최대 키를 기준으로 필터링한다.", example = "165"),
            @Parameter(name = "age", description = "나이대를 기준으로 필터링한다. (중복 가능)"),
            @Parameter(name = "category", description = "카테고리를 기준으로 필터링한다. (중복 가능)", example = "[\"셔츠\"]"),
            @Parameter(name = "style", description = "스타일을 기준으로 필터링한다. (중복 가능)", example = "[\"러블리\"]")
    })
    @GetMapping("")
    public ResponseEntity<BaseResponse<List<RentalListResponse>>> getRentalList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) List<Gender> gender,
            @RequestParam(required = false) Integer minHeight,
            @RequestParam(required = false) Integer maxHeight,
            @RequestParam(required = false) List<AgeFilter> age,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) List<String> style
    ) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.getRentalList(user, search, sort, gender,
                    minHeight, maxHeight, age, category, style)), OK);
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

    /* 대여글 수정 */
    @Operation(summary = "대여글 수정", description = "대여글을 수정한다.")
    @Parameter(name = "rentalId", description = "대여글 id", example = "1", required = true)
    @PutMapping(value = "/{rentalId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<RentalResponse>> updateRental(@Valid @RequestPart("post") RentalRequest rentalRequest,
                                                                         BindingResult bindingResult,
                                                                         @RequestPart(value = "images", required = false) MultipartFile[] images,
                                                                         @PathVariable Long rentalId,
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
                throw new BaseException(TOO_MANY_RENTAL_IMAGES, PAYLOAD_TOO_LARGE);
            }

            return new ResponseEntity<>(new BaseResponse<>(rentalService.updateRental(rentalRequest, images, userDetails.user, rentalId)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 대여글 삭제 */
    @Operation(summary = "대여글 삭제", description = "대여글을 삭제한다.")
    @Parameter(name = "rentalId", description = "대여글 id", example = "1", required = true)
    @DeleteMapping("/{rentalId}")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> deleteRental(@PathVariable("rentalId") Long rentalId,
                                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.deleteRental(rentalId, user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }
}
