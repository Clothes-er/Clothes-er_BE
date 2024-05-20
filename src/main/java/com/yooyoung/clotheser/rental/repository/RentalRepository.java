package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
}
