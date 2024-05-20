package com.yooyoung.clotheser.rental.service;

import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalPrice;
import com.yooyoung.clotheser.rental.dto.PostRentalRequest;
import com.yooyoung.clotheser.rental.dto.RentalResponse;
import com.yooyoung.clotheser.rental.repository.RentalPriceRepository;
import com.yooyoung.clotheser.rental.repository.RentalRepository;
import com.yooyoung.clotheser.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RentalPriceRepository rentalPriceRepository;

    // 대여글 생성
    public RentalResponse createRentalPost(PostRentalRequest postRentalRequest, Long clothesId, User user) {

        // TODO: 보유 옷으로부터 대여글 생성하는지 확인

        Rental rental = postRentalRequest.toEntity(user, clothesId);
        rentalRepository.save(rental);

        // TODO: 대여글 이미지 생성

        // 대여글 가격표 생성
        for (int i = 0; i < postRentalRequest.getPrices().size(); i++) {
            RentalPrice rentalPrice = RentalPrice.builder()
                    .rental(rental)
                    .price(postRentalRequest.getPrices().get(i).getPrice())
                    .days(postRentalRequest.getPrices().get(i).getDays())
                    .build();
            rentalPriceRepository.save(rentalPrice);
        }

        return new RentalResponse(user, rental, postRentalRequest.getPrices());

    }

}
