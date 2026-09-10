package com.back.nbe12142team06.domain.user.repository;


import com.back.nbe12142team06.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // username으로 튜플 검색
    Optional<User> findByUsername(String username);

    // DB에 username이 존재하는지 확인
    boolean existsByUsername(String username);
}
