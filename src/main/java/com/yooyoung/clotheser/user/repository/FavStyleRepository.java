package com.yooyoung.clotheser.user.repository;

import com.yooyoung.clotheser.user.domain.FavStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavStyleRepository extends JpaRepository<FavStyle, Long> {

    List<FavStyle> findAllByUserId(Long userId);
    void deleteAllByUserId(Long userId);

}
