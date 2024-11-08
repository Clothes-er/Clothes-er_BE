package com.yooyoung.clotheser.closet.service;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.closet.dto.*;
import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.clothes.domain.ClothesImg;
import com.yooyoung.clotheser.clothes.dto.ClothesListResponse;
import com.yooyoung.clotheser.clothes.repository.ClothesImgRepository;
import com.yooyoung.clotheser.clothes.repository.ClothesLikeRepository;
import com.yooyoung.clotheser.clothes.repository.ClothesRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalPrice;
import com.yooyoung.clotheser.rental.dto.response.RentalListResponse;
import com.yooyoung.clotheser.rental.repository.*;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ClosetService {

    private final AESUtil aesUtil;

    private final UserRepository userRepository;

    private final RentalRepository rentalRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalPriceRepository rentalPriceRepository;
    private final RentalInfoRepository rentalInfoRepository;
    private final RentalLikeRepository rentalLikeRepository;

    private final ClothesRepository clothesRepository;
    private final ClothesImgRepository clothesImgRepository;
    private final ClothesLikeRepository clothesLikeRepository;

    private final ChatRoomRepository chatRoomRepository;

    /* 나의 보유 옷 목록 조회 */
    public List<UserClothesListResponse> getMyClothes(User user) throws BaseException {
        user.checkIsFirstLogin();

        // 나의 전체 보유 옷 목록 불러오기
        List<Clothes> myClothes = clothesRepository.findAllByUserIdAndDeletedAtNullOrderByCreatedAtDesc(user.getId());
        List<UserClothesListResponse> responses = new ArrayList<>();
        for (Clothes clothes : myClothes) {
            // 첫 번째 이미지 불러오기
            Optional<ClothesImg> optionalImg = clothesImgRepository.findFirstByClothesId(clothes.getId());
            String imgUrl = optionalImg.map(ClothesImg::getImgUrl).orElse(null);

            responses.add(new UserClothesListResponse(clothes, imgUrl));
        }

        return responses;
    }

    /* 남의 보유 옷 목록 조회 */
    public List<UserClothesListResponse> getUserClothes(User user, String userSid) throws BaseException {
        user.checkIsFirstLogin();

        // 조회하려는 회원 불러오기
        Long userId = aesUtil.decryptUserSid(userSid);
        User owner = userRepository.findByIdAndDeletedAtNull(userId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER, NOT_FOUND));

        // 남의 전체 보유 옷 목록 불러오기
        List<Clothes> userClothes = clothesRepository.findAllByUserIdAndDeletedAtNullAndIsPublicTrueOrderByCreatedAtDesc(owner.getId());
        List<UserClothesListResponse> responses = new ArrayList<>();
        for (Clothes clothes : userClothes) {
            // 첫 번째 이미지 불러오기
            Optional<ClothesImg> optionalImg = clothesImgRepository.findFirstByClothesId(clothes.getId());
            String imgUrl = optionalImg.map(ClothesImg::getImgUrl).orElse(null);

            responses.add(new UserClothesListResponse(clothes, imgUrl));
        }

        return responses;
    }

    /* 나의 대여글 목록 조회 */
    public List<UserRentalListResponse> getMyRentals(User user) throws BaseException {
        user.checkIsFirstLogin();

        // 나의 전체 대여글 목록 불러오기
        List<Rental> myRentals = rentalRepository.findAllByUserIdAndDeletedAtNullOrderByCreatedAtDesc(user.getId());

        List<UserRentalListResponse> responses = new ArrayList<>();
        for (Rental rental : myRentals) {
            // 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 가격 정보 중에 제일 싼 가격 및 일수 불러오기
            int minPrice = 0;
            int minDays = 0;
            Optional<RentalPrice> minRentalPrice = rentalPriceRepository.findFirstByRentalOrderByPriceAscDaysAsc(rental);
            if (minRentalPrice.isPresent()) {
                minPrice = minRentalPrice.get().getPrice();
                minDays = minRentalPrice.get().getDays();
            }

            responses.add(new UserRentalListResponse(rental, imgUrl, minPrice, minDays));
        }

        return responses;
    }

    /* 남의 대여글 목록 조회 */
    public List<UserRentalListResponse> getUserRentals(User user, String userSid) throws BaseException {
        user.checkIsFirstLogin();

        // 조회하려는 회원 불러오기
        Long userId = aesUtil.decryptUserSid(userSid);
        User owner = userRepository.findByIdAndDeletedAtNull(userId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER, NOT_FOUND));

        // 남의 전체 대여글 목록 불러오기
        List<Rental> myRentals = rentalRepository.findAllByUserIdAndDeletedAtNullOrderByCreatedAtDesc(owner.getId());

        List<UserRentalListResponse> responses = new ArrayList<>();
        for (Rental rental : myRentals) {
            // 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 가격 정보 중에 제일 싼 가격 및 일수 불러오기
            int minPrice = 0;
            int minDays = 0;
            Optional<RentalPrice> minRentalPrice = rentalPriceRepository.findFirstByRentalOrderByPriceAscDaysAsc(rental);
            if (minRentalPrice.isPresent()) {
                minPrice = minRentalPrice.get().getPrice();
                minDays = minRentalPrice.get().getDays();
            }

            responses.add(new UserRentalListResponse(rental, imgUrl, minPrice, minDays));
        }

        return responses;

    }

    /* 공유 내역 조회 */
    public List<RentalHistoryResponse> getShareHistory(User user) throws BaseException {
        user.checkIsFirstLogin();

        // 대여 중인 대여글 먼저 조회 후 대여 완료된 대여글 보여주기
        List<RentalInfo> rentalInfoList = rentalInfoRepository.findAllByLenderIdOrderByStateAndRentalDate(user.getId());
        List<RentalHistoryResponse> responses = new ArrayList<>();

        for (RentalInfo rentalInfo : rentalInfoList) {
            Rental rental = rentalInfo.getRental();

            // 채팅방 불러오기
            Long roomId = null;
            ChatRoom chatRoom = chatRoomRepository.findOneByBuyerIdAndLenderIdAndRentalId(
                    rentalInfo.getBuyer().getId(),
                    rentalInfo.getLender().getId(),
                    rental.getId()
            ).orElse(null);
            if (chatRoom != null) {
                roomId = chatRoom.getId();
            }

            // userId 암호화하기
            String userSid;
            try {
                String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()));
                userSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
            }

            // 대여자 구하기
            User buyer = rentalInfo.getBuyer();

            // 첫 번째 이미지 URL 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 가격 정보 중에 제일 싼 가격 및 일수 불러오기
            int minPrice = 0;
            int minDays = 0;
            Optional<RentalPrice> minRentalPrice = rentalPriceRepository.findFirstByRentalOrderByPriceAscDaysAsc(rental);
            if (minRentalPrice.isPresent()) {
                minPrice = minRentalPrice.get().getPrice();
                minDays = minRentalPrice.get().getDays();
            }

            // RentalHistoryResponse 객체 생성 및 리스트에 추가
            RentalHistoryResponse response = new RentalHistoryResponse(rental, roomId, userSid, imgUrl, buyer, minPrice, minDays, rentalInfo);
            responses.add(response);
        }

        return responses;
    }

    /* 대여 내역 조회 */
    public List<RentalHistoryResponse> getRentalHistory(User user) throws BaseException {
        user.checkIsFirstLogin();

        // 대여 중인 대여글 먼저 조회 후 대여 완료된 대여글 보여주기
        List<RentalInfo> rentalInfoList = rentalInfoRepository.findAllByBuyerIdOrderByStateAndRentalDate(user.getId());
        List<RentalHistoryResponse> responses = new ArrayList<>();

        for (RentalInfo rentalInfo : rentalInfoList) {
            Rental rental = rentalInfo.getRental();

            // 채팅방 불러오기
            Long roomId = null;
            ChatRoom chatRoom = chatRoomRepository.findOneByBuyerIdAndLenderIdAndRentalId(
                    rentalInfo.getBuyer().getId(),
                    rentalInfo.getLender().getId(),
                    rental.getId()
            ).orElse(null);
            if (chatRoom != null) {
                roomId = chatRoom.getId();
            }

            // userId 암호화하기
            String userSid;
            try {
                String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()));
                userSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
            }

            // 작성자 구하기
            User writer = rental.getUser();

            // 첫 번째 이미지 URL 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 가격 정보 중에 제일 싼 가격 및 일수 불러오기
            int minPrice = 0;
            int minDays = 0;
            Optional<RentalPrice> minRentalPrice = rentalPriceRepository.findFirstByRentalOrderByPriceAscDaysAsc(rental);
            if (minRentalPrice.isPresent()) {
                minPrice = minRentalPrice.get().getPrice();
                minDays = minRentalPrice.get().getDays();
            }

            // RentalHistoryResponse 객체 생성 및 리스트에 추가
            RentalHistoryResponse response = new RentalHistoryResponse(rental, roomId, userSid, imgUrl, writer, minPrice, minDays, rentalInfo);
            responses.add(response);
        }

        return responses;
    }

    /* 나의 보유 옷 찜 목록 조회*/
    public List<LikeClothesListResponse> getMyLikeClothesList(User user) throws BaseException {
        user.checkIsFirstLogin();

        List<Clothes> myLikeClothes = clothesRepository.findLikeClothes(user.getId());
        List<LikeClothesListResponse> responses = new ArrayList<>();
        for (Clothes clothes : myLikeClothes) {
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

            ClothesListResponse clothesListResponse = new ClothesListResponse(clothes, userSid, imgUrl);
            boolean isLiked = clothesLikeRepository.existsByUserIdAndClothesIdAndDeletedAtNull(user.getId(), clothes.getId());

            responses.add(new LikeClothesListResponse(clothesListResponse, isLiked));
        }

        return responses;
    }

    /* 나의 대여글 찜 목록 조회*/
    public List<LikeRentalListResponse> getMyLikeRentals(User user) throws BaseException {
        user.checkIsFirstLogin();

        List<Rental> myLikeRentals = rentalRepository.findLikeRentals(user.getId());
        List<LikeRentalListResponse> responses = new ArrayList<>();
        for (Rental rental : myLikeRentals) {
            // userId 암호화하기
            String userSid;
            try {
                String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()));
                userSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
            }

            // 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 가격 정보 중에 제일 싼 가격 및 일수 불러오기
            int minPrice = 0;
            int minDays = 0;
            Optional<RentalPrice> minRentalPrice = rentalPriceRepository.findFirstByRentalOrderByPriceAscDaysAsc(rental);
            if (minRentalPrice.isPresent()) {
                minPrice = minRentalPrice.get().getPrice();
                minDays = minRentalPrice.get().getDays();
            }

            RentalListResponse rentalListResponse = new RentalListResponse(rental, userSid, imgUrl, minPrice, minDays);
            boolean isLiked = rentalLikeRepository.existsByUserIdAndRentalIdAndDeletedAtNull(user.getId(), rental.getId());
            responses.add(new LikeRentalListResponse(rentalListResponse, isLiked));
        }

        return responses;
    }
}
