package com.back.nbe12142team06.domain.review.repository;

import com.back.nbe12142team06.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);

    // 특정 동행인이 받은 리뷰 목록 ( Application -> Escort )
    List<Review> findByApplication_Escort_Id(Long escortId);
}
