package com.yooyoung.clotheser.closet.service;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.closet.dto.RentalHistoryResponse;
import com.yooyoung.clotheser.closet.dto.UserClothesListResponse;
import com.yooyoung.clotheser.closet.dto.UserRentalListResponse;
import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.clothes.domain.ClothesImg;
import com.yooyoung.clotheser.clothes.repository.ClothesImgRepository;
import com.yooyoung.clotheser.clothes.repository.ClothesRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalPrice;
import com.yooyoung.clotheser.rental.repository.RentalImgRepository;
import com.yooyoung.clotheser.rental.repository.RentalInfoRepository;
import com.yooyoung.clotheser.rental.repository.RentalPriceRepository;
import com.yooyoung.clotheser.rental.repository.RentalRepository;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private AESUtil aesUtil;
    @Value("${aes.key}")
    private String AES_KEY;

    private final UserRepository userRepository;

    private final RentalRepository rentalRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalPriceRepository rentalPriceRepository;
    private final RentalInfoRepository rentalInfoRepository;

    private final ClothesRepository clothesRepository;
    private final ClothesImgRepository clothesImgRepository;

    private final ChatRoomRepository chatRoomRepository;

    /* 나의 전체 보유 옷 목록 조회 */
    public List<UserClothesListResponse> getMyClothes(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

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

    /* 남의 전체 보유 옷 목록 조회 */
    public List<UserClothesListResponse> getUserClothes(User user, String userSid) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 조회하려는 회원 불러오기
        long userId;
        try {
            String base64DecodedUserId = Base64UrlSafeUtil.decode(userSid);
            userId = Long.parseLong(aesUtil.decrypt(base64DecodedUserId, AES_KEY));
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_DECRYPT, INTERNAL_SERVER_ERROR);
        }
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

    /* 나의 전체 대여글 목록 조회 */
    public List<UserRentalListResponse> getMyRentals(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

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

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 조회하려는 회원 불러오기
        long userId;
        try {
            String base64DecodedUserId = Base64UrlSafeUtil.decode(userSid);
            userId = Long.parseLong(aesUtil.decrypt(base64DecodedUserId, AES_KEY));
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_DECRYPT, INTERNAL_SERVER_ERROR);
        }
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

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

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
                String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()), AES_KEY);
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

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

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
                String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()), AES_KEY);
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

}
