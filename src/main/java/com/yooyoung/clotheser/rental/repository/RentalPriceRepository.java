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

    List<RentalPrice> findAllByRentalId(Long rentalId);

    @Query("select min(rp.price) from RentalPrice rp where rp.rental = :rental")
    Optional<Integer> findMinPrice(Rental rental);
}
