package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.RentalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RentalInfoRepository extends JpaRepository<RentalInfo, Long> {

    Optional<RentalInfo> findByRentalId(Long rentalId);

}
