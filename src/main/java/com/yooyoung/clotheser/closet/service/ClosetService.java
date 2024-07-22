package com.yooyoung.clotheser.closet.service;

import com.yooyoung.clotheser.closet.dto.RentalHistoryResponse;
import com.yooyoung.clotheser.closet.dto.UserRentalListResponse;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
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

    private final RentalRepository rentalRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalPriceRepository rentalPriceRepository;

    private final UserRepository userRepository;
    @Autowired
    private RentalInfoRepository rentalInfoRepository;

    // 나의 전체 대여글 목록 조회
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

            // 가격 정보 중에 제일 싼 가격 불러오기
            Optional<Integer> optionalPrice = rentalPriceRepository.findMinPrice(rental);
            int minPrice = optionalPrice.orElse(0);

            responses.add(new UserRentalListResponse(rental, imgUrl, minPrice));
        }

        return responses;
    }

    // 남의 대여글 목록 조회
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

            // 가격 정보 중에 제일 싼 가격 불러오기
            Optional<Integer> optionalPrice = rentalPriceRepository.findMinPrice(rental);
            int minPrice = optionalPrice.orElse(0);

            responses.add(new UserRentalListResponse(rental, imgUrl, minPrice));
        }

        return responses;

    }

    // 공유 내역 조회
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

            // userId 암호화하기
            String userSid;
            try {
                String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()), AES_KEY);
                userSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
            }

            // 대여자 닉네임 구하기
            String nickname = rentalInfo.getBuyer().getNickname();

            // 첫 번째 이미지 URL 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 최소 가격 불러오기
            Optional<Integer> optionalPrice = rentalPriceRepository.findMinPrice(rental);
            int minPrice = optionalPrice.orElse(0);

            // RentalHistoryResponse 객체 생성 및 리스트에 추가
            RentalHistoryResponse response = new RentalHistoryResponse(rental, userSid, imgUrl, nickname, minPrice, rentalInfo);
            responses.add(response);
        }

        return responses;
    }

    // 대여 내역 조회
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

            // userId 암호화하기
            String userSid;
            try {
                String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()), AES_KEY);
                userSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
            }

            // 작성자 닉네임 구하기
            String nickname = rental.getUser().getNickname();

            // 첫 번째 이미지 URL 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 최소 가격 불러오기
            Optional<Integer> optionalPrice = rentalPriceRepository.findMinPrice(rental);
            int minPrice = optionalPrice.orElse(0);

            // RentalHistoryResponse 객체 생성 및 리스트에 추가
            RentalHistoryResponse response = new RentalHistoryResponse(rental, userSid, imgUrl, nickname, minPrice, rentalInfo);
            responses.add(response);
        }

        return responses;
    }

}