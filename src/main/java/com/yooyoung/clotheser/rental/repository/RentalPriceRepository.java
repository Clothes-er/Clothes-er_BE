package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.RentalPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalPriceRepository extends JpaRepository<RentalPrice, Long> {

}
