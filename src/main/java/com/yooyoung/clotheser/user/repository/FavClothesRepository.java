package com.yooyoung.clotheser.user.repository;

import com.yooyoung.clotheser.user.domain.FavClothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavClothesRepository extends JpaRepository<FavClothes,Long> {

    List<FavClothes> findAllByUserId(Long userId);
    void deleteAllByUserId(Long userId);

}
