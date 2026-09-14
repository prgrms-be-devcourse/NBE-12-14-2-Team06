package com.back.nbe12142team06.domain.application.repository;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // 중복 지원 방지(post+escort)
    boolean existsByPostAndEscort(Post post, User escort);
}