package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    Optional<Rental> findByIdAndDeletedAtNull(Long id);

    // 반경 2km 이내 대여글 목록 최신순으로 불러오기
    @Query(value = "SELECT r.* FROM rental r JOIN user u ON r.user_id = u.id " +
            "WHERE ST_Distance_Sphere(point(u.longitude, u.latitude), point(:longitude, :latitude)) <= 2000 " +
            "AND r.deleted_at is null " +
            "ORDER BY r.created_at DESC",
            nativeQuery = true)
    List<Rental> findRentalsWithinDistance(@Param("latitude") double latitude, @Param("longitude") double longitude);

}
