package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalPriceRepository extends JpaRepository<RentalPrice, Long> {

    // 기간 적은 순으로 가격 정보 불러오기
    List<RentalPrice> findAllByRentalIdOrderByDays(Long rentalId);

    // 최소 가격과 일수 불러오기
    @Query("select rp from RentalPrice rp " +
            "where rp.rental = :rental and rp.price = " +
            "(select min(rp2.price) from RentalPrice rp2 where rp2.rental = :rental)")
    Optional<RentalPrice> findMinPriceAndDays(Rental rental);

}
