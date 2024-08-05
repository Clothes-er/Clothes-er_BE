package com.yooyoung.clotheser.clothes.service;

import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.clothes.dto.ClothesRequest;
import com.yooyoung.clotheser.clothes.dto.ClothesResponse;
import com.yooyoung.clotheser.clothes.repository.ClothesImgRepository;
import com.yooyoung.clotheser.clothes.repository.ClothesRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.repository.RentalRepository;
import com.yooyoung.clotheser.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ClothesService {

    @Autowired
    private AESUtil aesUtil;
    @Value("${aes.key}")
    private String AES_KEY;

    private final ClothesImageService clothesImageService;
    
    private final ClothesRepository clothesRepository;
    private final ClothesImgRepository clothesImgRepository;
    private final RentalRepository rentalRepository;

    /* 보유 옷 생성 */
    public ClothesResponse createClothes(ClothesRequest clothesRequest, MultipartFile[] images, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 기존 대여글과 연결할 경우
        Long rentalId = clothesRequest.getRentalId();
        boolean hasRental = false;
        Rental rental = null;
        if (rentalId != null && rentalId > 0) {
            // 대여글 존재 확인
            rental = rentalRepository.findByIdAndDeletedAtNull(rentalId)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));

            // 대여글 회원과 보유 옷 등록하려는 회원 일치 확인
            if (!rental.getUser().getId().equals(user.getId())) {
                throw new BaseException(FORBIDDEN_CREATE_CLOTHES, FORBIDDEN);
            }

            hasRental = true;
        }

        // 보유 옷 저장
        Clothes clothes = clothesRequest.toEntity(user);
        clothesRepository.save(clothes);

        // 대여글 있는 경우 보유 옷 id 저장
        if (hasRental) {
            Rental updatedRental = rental.updateClothes(clothes.getId());
            rentalRepository.save(updatedRental);
        }

        // 보유 옷 이미지 저장
        List<String> imgUrls = clothesImageService.uploadClothesImages(images, clothes);

        // 본인의 id 암호화하기
        String userSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(user.getId()), AES_KEY);
            userSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new ClothesResponse(user, userSid, clothes, imgUrls);
    }

    /* 보유 옷 조회 */
    public ClothesResponse getClothes(Long clothesId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 보유 옷 불러오기
        Clothes clothes = clothesRepository.findByIdAndDeletedAtNull(clothesId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CLOTHES, NOT_FOUND));

        // 보유 옷 이미지 불러오기
        List<String> imgUrls = clothesImgRepository.findImgUrlsByClothesId(clothesId);

        // 보유 옷 등록자의 id 암호화하기
        String userSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(clothes.getUser().getId()), AES_KEY);
            userSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new ClothesResponse(user, userSid, clothes, imgUrls);

    }

}
