package com.yooyoung.clotheser.clothes.controller;

import com.yooyoung.clotheser.closet.dto.UserRentalListResponse;
import com.yooyoung.clotheser.clothes.dto.ClothesListResponse;
import com.yooyoung.clotheser.clothes.dto.ClothesRequest;
import com.yooyoung.clotheser.clothes.dto.ClothesResponse;
import com.yooyoung.clotheser.clothes.service.ClothesService;
import com.yooyoung.clotheser.global.entity.AgeFilter;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
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
@RequestMapping("/api/v1/clothes")
@Tag(name = "Clothes", description = "옷장 구경 API")
public class ClothesController {

    private final ClothesService clothesService;
    private final RentalService rentalService;

    /* 보유 옷이 없는 나의 대여글 목록 조회 (보유 옷 생성 전 사용) */
    @Operation(summary = "보유 옷이 없는 나의 대여글 목록 조회", description = "보유 옷 등록 전에 보유 옷이 없는 나의 대여글 목록을 조회한다.")
    @GetMapping("/my-rentals")
    public ResponseEntity<BaseResponse<List<UserRentalListResponse>>> getMyNoClothesRentals(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(rentalService.getMyNoClothesRentals(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 보유 옷 생성 */
    @Operation(summary = "보유 옷 생성", description = "보유 옷을 생성한다.")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ClothesResponse>> createClothes(@Valid @RequestPart("clothes") ClothesRequest clothesRequest,
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

            // 보유 옷 이미지 최대 3장
            if (images.length > 3) {
                throw new BaseException(TOO_MANY_CLOTHES_IMAGES, PAYLOAD_TOO_LARGE);
            }

            return new ResponseEntity<>(new BaseResponse<>(clothesService.createClothes(clothesRequest, images, userDetails.user)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 보유 옷 조회 */
    @Operation(summary = "보유 옷 조회", description = "보유 옷의 상세 정보를 조회한다.")
    @Parameter(name = "clothesId", description = "보유 옷 id", example = "1", required = true)
    @GetMapping("/{clothesId}")
    public ResponseEntity<BaseResponse<ClothesResponse>> getClothes(@PathVariable Long clothesId,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(clothesService.getClothes(clothesId, user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 보유 옷 수정 */
    @Operation(summary = "보유 옷 수정", description = "보유 옷을 수정한다.")
    @Parameter(name = "clothesId", description = "보유 옷 id", example = "1", required = true)
    @PutMapping(value = "/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ClothesResponse>> updateClothes(@Valid @RequestPart("clothes") ClothesRequest clothesRequest,
                                                                     BindingResult bindingResult,
                                                                     @RequestPart(value = "images", required = false) MultipartFile[] images,
                                                                     @PathVariable Long clothesId,
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

            // 보유 옷 이미지 최대 3장
            if (images.length > 3) {
                throw new BaseException(TOO_MANY_CLOTHES_IMAGES, PAYLOAD_TOO_LARGE);
            }

            return new ResponseEntity<>(new BaseResponse<>(clothesService.updateClothes(clothesRequest, images, userDetails.user, clothesId)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 보유 옷 삭제 */
    @Operation(summary = "보유 옷 삭제", description = "보유 옷을 삭제한다.")
    @Parameter(name = "clothesId", description = "보유 옷 id", example = "1", required = true)
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> deleteClothes(@PathVariable Long clothesId,
                                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(clothesService.deleteClothes(clothesId, user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    /* 보유 옷 목록 조회 */
    @Operation(summary = "보유 옷 목록 조회", description = "회원의 성별, 카테고리, 스타일 유사도 기준으로 보유 옷 목록을 조회한다.")
    @Parameters({
            @Parameter(name = "search", description = "상품명을 기준으로 대소문자 구분 없이 검색한다.", example = "블라우스"),
            @Parameter(name = "sort", description = "특정 기준으로 정렬한다.", example = "createdAt"),
            @Parameter(name = "gender", description = "성별을 기준으로 필터링한다. (중복 가능)"),
            @Parameter(name = "minHeight", description = "최소 키를 기준으로 필터링한다.", example = "158"),
            @Parameter(name = "maxHeight", description = "최대 키를 기준으로 필터링한다.", example = "165"),
            @Parameter(name = "age", description = "나이대를 기준으로 필터링한다. (중복 가능)"),
            @Parameter(name = "category", description = "카테고리를 기준으로 필터링한다. (중복 가능)", example = "[\"셔츠\"]"),
            @Parameter(name = "style", description = "스타일을 기준으로 필터링한다. (중복 가능)", example = "[\"러블리\"]")
    })
    @GetMapping("")
    public ResponseEntity<BaseResponse<List<ClothesListResponse>>> getClothes(
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
            return new ResponseEntity<>(new BaseResponse<>(clothesService.getClothesList(user, search, sort, gender,
                    minHeight, maxHeight, age, category, style)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
