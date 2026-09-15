package com.back.nbe12142team06.domain.auth.repository;

import com.back.nbe12142team06.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // 토큰의 해시값으로 조회
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    // List<RefreshToken> findAllByUserAndRevokedAtIsNull(User user);
}
