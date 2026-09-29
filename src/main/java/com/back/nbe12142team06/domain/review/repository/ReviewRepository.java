package com.back.nbe12142team06.domain.review.repository;

import com.back.nbe12142team06.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);

    // 특정 동행인이 받은 리뷰 목록 (Review -> Application -> Escort)
    // tags 는 @ElementCollection 이라 @BatchSize 로 별도 처리
    @Query("""
            SELECT r
            FROM Review r
            JOIN FETCH r.application a
            JOIN FETCH a.escort
            WHERE a.escort.id = :escortId
            ORDER BY r.createdAt DESC
            """)
    List<Review> findAllByEscortIdWithApplication(@Param("escortId") Long escortId);
}