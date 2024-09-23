package com.yooyoung.clotheser.clothes.service;

import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.clothes.domain.ClothesImg;
import com.yooyoung.clotheser.clothes.dto.ClothesListResponse;
import com.yooyoung.clotheser.clothes.dto.ClothesRequest;
import com.yooyoung.clotheser.clothes.dto.ClothesResponse;
import com.yooyoung.clotheser.clothes.repository.ClothesImgRepository;
import com.yooyoung.clotheser.clothes.repository.ClothesRepository;

import com.yooyoung.clotheser.global.entity.AgeFilter;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;

import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.dto.response.NoRentalClothesListResponse;
import com.yooyoung.clotheser.rental.repository.RentalRepository;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ClothesService {

    private final AESUtil aesUtil;

    private final ClothesImageService clothesImageService;
    private final ClothesFilterService clothesFilterService;
    
    private final ClothesRepository clothesRepository;
    private final ClothesImgRepository clothesImgRepository;
    private final RentalRepository rentalRepository;

    /* 대여글이 없는 나의 보유 옷 목록 조회 */
    public List<NoRentalClothesListResponse> getMyNoRentalClothes(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 유예된 회원 확인
        if (user.getIsSuspended()) {
            throw new BaseException(USE_RESTRICTED, FORBIDDEN);
        }

        // 보유 옷 목록 불러오기
        List<Clothes> myClothes = clothesRepository.findAllByUserIdAndRentalIdNullAndDeletedAtNullOrderByCreatedAtDesc(user.getId());
        List<NoRentalClothesListResponse> responses = new ArrayList<>();
        for (Clothes clothes : myClothes) {
            // 첫 번째 이미지 불러오기
            Optional<ClothesImg> optionalImg = clothesImgRepository.findFirstByClothesId(clothes.getId());
            String imgUrl = optionalImg.map(ClothesImg::getImgUrl).orElse(null);

            responses.add(new NoRentalClothesListResponse(clothes, imgUrl));
        }

        return responses;
    }

    /* 보유 옷 생성 */
    public ClothesResponse createClothes(ClothesRequest clothesRequest, MultipartFile[] images, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 유예된 회원 확인
        if (user.getIsSuspended()) {
            throw new BaseException(USE_RESTRICTED, FORBIDDEN);
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

            // 이미 연동된 대여글인지 확인
            if (rental.getClothesId() != null) {
                throw new BaseException(RENTAL_HAS_CLOTHES, CONFLICT);
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
            String encodedUserId = aesUtil.encrypt(String.valueOf(user.getId()));
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
        List<ClothesImg> clothesImgs = clothesImgRepository.findAllByClothesId(clothesId);
        List<String> imgUrls = clothesImgs.stream()
                .map(ClothesImg::getImgUrl)
                .toList();

        // 보유 옷 등록자의 id 암호화하기
        String userSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(clothes.getUser().getId()));
            userSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new ClothesResponse(user, userSid, clothes, imgUrls);
    }

    /* 보유 옷 수정 */
    public ClothesResponse updateClothes(ClothesRequest clothesRequest, MultipartFile[] images, User user, Long clothesId) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 유예된 회원 확인
        if (user.getIsSuspended()) {
            throw new BaseException(USE_RESTRICTED, FORBIDDEN);
        }

        // 보유 옷 불러오기
        Clothes clothes = clothesRepository.findByIdAndDeletedAtNull(clothesId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CLOTHES, NOT_FOUND));

        // 본인의 보유 옷인지 확인
        if (!user.getId().equals(clothes.getUser().getId())) {
            throw new BaseException(FORBIDDEN_USER, FORBIDDEN);
        }

        // 대여글 연결을 변경하는 경우
        Long originalRentalId = clothes.getRentalId();
        Rental originalRental = rentalRepository.findByIdAndDeletedAtNull(originalRentalId).orElse(null);

        Long newRentalId = clothesRequest.getRentalId();
        Rental newRental = null;
        if (newRentalId != null && newRentalId > 0) {
            // 대여글 존재 확인
            newRental = rentalRepository.findByIdAndDeletedAtNull(newRentalId)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));

            // 대여글 작성자와 보유 글 등록자 일치 확인
            if(!newRental.getUser().getId().equals(user.getId())) {
                throw new BaseException(FORBIDDEN_UPDATE_CLOTHES, FORBIDDEN);
            }
        }

        // 기존 대여글이 있었으면 보유 옷 연결 끊기
        if (originalRental != null) {
            originalRental = originalRental.updateClothes(null);
            rentalRepository.save(originalRental);
        }
        // 새로운 대여글이 있으면 보유 옷 연결
        if (newRental != null) {
            newRental = newRental.updateClothes(clothes.getId());
            rentalRepository.save(newRental);
        }

        // 보유 옷 이미지 변경
        List<ClothesImg> clothesImgs = clothesImgRepository.findAllByClothesId(clothesId);
        clothesImageService.deleteClothesImages(clothesImgs);
        List<String> imgUrls = clothesImageService.uploadClothesImages(images, clothes);

        // 보유 옷 수정
        Clothes updatedClothes = clothes.updateClothes(clothesRequest, user);
        clothesRepository.save(updatedClothes);

        // 본인의 id 암호화하기
        String userSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(user.getId()));
            userSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new ClothesResponse(user, userSid, updatedClothes, imgUrls);

    }

    /* 보유 옷 삭제 */
    public BaseResponseStatus deleteClothes(Long clothesId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 유예된 회원 확인
        if (user.getIsSuspended()) {
            throw new BaseException(USE_RESTRICTED, FORBIDDEN);
        }

        // 보유 옷 불러오기
        Clothes clothes = clothesRepository.findByIdAndDeletedAtNull(clothesId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CLOTHES, NOT_FOUND));

        // 본인의 보유 옷인지 확인
        if (!user.getId().equals(clothes.getUser().getId())) {
            throw new BaseException(FORBIDDEN_USER, FORBIDDEN);
        }

        // 보유 옷 이미지 물리 삭제
        List<ClothesImg> clothesImgs = clothesImgRepository.findAllByClothesId(clothesId);
        clothesImageService.deleteClothesImages(clothesImgs);

        // 보유 옷 논리 삭제
        Clothes deletedClothes = clothes.deleteClothes();
        clothesRepository.save(deletedClothes);

        // 보유 옷에 연결된 대여글의 clothesId 삭제
        Long rentalId = deletedClothes.getRentalId();
        if (rentalId != null) {
            Rental rental = rentalRepository.findByIdAndDeletedAtNull(rentalId)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));
            Rental updatedRental = rental.updateClothes(null);
            rentalRepository.save(updatedRental);
        }

        return SUCCESS;
    }

    /* 보유 옷 목록 조회 */
    public List<ClothesListResponse> getClothesList(User user, String search, String sort, List<Gender> gender, Integer minHeight,
                                                   Integer maxHeight, List<AgeFilter> age, List<String> category, List<String> style) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 필터링된 보유 옷 목록 조회
        List<Clothes> clothesList = clothesFilterService.getFilteredClothesList(user, search, sort, gender,
                minHeight, maxHeight, age, category, style);

        List<ClothesListResponse> responses = new ArrayList<>();
        for (Clothes clothes : clothesList) {
            // userId 암호화하기
            String userSid;
            try {
                String encodedUserId = aesUtil.encrypt(String.valueOf(clothes.getUser().getId()));
                userSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
            }

            // 첫 번째 이미지 불러오기
            Optional<ClothesImg> optionalImg = clothesImgRepository.findFirstByClothesId(clothes.getId());
            String imgUrl = optionalImg.map(ClothesImg::getImgUrl).orElse(null);

            responses.add(new ClothesListResponse(clothes, userSid, imgUrl));
        }

        return responses;

    }

}
