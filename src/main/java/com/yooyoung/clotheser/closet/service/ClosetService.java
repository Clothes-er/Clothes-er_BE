package com.yooyoung.clotheser.closet.service;

import com.yooyoung.clotheser.closet.dto.UserRentalListResponse;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.repository.RentalImgRepository;
import com.yooyoung.clotheser.rental.repository.RentalPriceRepository;
import com.yooyoung.clotheser.rental.repository.RentalRepository;
import com.yooyoung.clotheser.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.REQUEST_FIRST_LOGIN;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@Transactional
@RequiredArgsConstructor
public class ClosetService {

    private final RentalRepository rentalRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalPriceRepository rentalPriceRepository;

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

}
