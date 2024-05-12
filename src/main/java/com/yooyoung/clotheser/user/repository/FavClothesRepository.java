package com.yooyoung.clotheser.user.repository;

import com.yooyoung.clotheser.user.domain.FavClothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavClothesRepository extends JpaRepository<FavClothes,Long> {
}
