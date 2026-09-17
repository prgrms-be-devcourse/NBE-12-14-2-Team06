package com.back.nbe12142team06.domain.application.repository;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // 중복 지원 방지(post+escort)
    boolean existsByPostAndEscort(Post post, User escort);

    // 공고별 지원 목록 조회
    @Query("""
        SELECT a
        FROM Application a
        JOIN FETCH a.escort
        WHERE a.post.id = :postId
        """)
    List<Application> findAllByPostIdWithEscort(@Param("postId") Long postId);

    // 페이징 조회용
    @Query("""
        SELECT a
        FROM Application a
        JOIN FETCH a.escort
        WHERE a.post.id = :postId
        ORDER BY a.id DESC
        """)
    Page<Application> findAllByPostIdWithEscort(
            @Param("postId") Long postId,
            Pageable pageable
    );

    List<Application> findAllByPostAndStatus(Post post, ApplicationStatus status);

    // 같은 동행인의 특정 상태 지원 조회
    List<Application> findAllByEscortAndStatus(User escort, ApplicationStatus status);
}