package com.back.nbe12142team06.domain.application.repository;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.user.entity.User;
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
}