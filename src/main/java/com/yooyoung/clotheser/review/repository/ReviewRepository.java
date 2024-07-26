package com.yooyoung.clotheser.review.repository;

import com.yooyoung.clotheser.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 기존에 후기를 작성했었는지 확인
    boolean existsByRentalInfoIdAndReviewerId(Long rentalInfoId, Long reviewerId);

    // 받은 후기 건수 확인
    int countByRevieweeId(Long revieweeId);

    // 최근 후기 3건 가져오기
    List<Review> findTop3ByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    // 받은 후기 모두 가져오기
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

}
