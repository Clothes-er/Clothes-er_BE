package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.RentalCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalCheckRepository extends JpaRepository<RentalCheck, Long> {

    boolean existsByRoomId(Long roomId);

    List<RentalCheck> findAllByRoomId(Long roomId);

}
