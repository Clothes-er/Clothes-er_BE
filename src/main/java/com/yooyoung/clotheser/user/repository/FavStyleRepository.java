package com.yooyoung.clotheser.user.repository;

import com.yooyoung.clotheser.user.domain.FavStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavStyleRepository extends JpaRepository<FavStyle, Long> {
}
