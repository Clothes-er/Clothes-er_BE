package com.yooyoung.clotheser.clothes.controller;

import com.yooyoung.clotheser.clothes.dto.ClothesRequest;
import com.yooyoung.clotheser.clothes.dto.ClothesResponse;
import com.yooyoung.clotheser.clothes.service.ClothesService;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

}
