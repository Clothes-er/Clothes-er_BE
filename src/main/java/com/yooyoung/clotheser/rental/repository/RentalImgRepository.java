package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.RentalImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalImgRepository extends JpaRepository<RentalImg, Long> {

    Optional<RentalImg> findFirstByRentalId(Long rentalId);

}
