package com.back.nbe12142team06.domain.auth.service;

import com.back.nbe12142team06.domain.auth.entity.RefreshToken;
import com.back.nbe12142team06.domain.auth.repository.RefreshTokenRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.exception.UnauthorizedException;
import com.back.nbe12142team06.global.security.RefreshTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${custom.jwt.refresh-expire-seconds}")
    private Long refreshExpireSeconds;


    // 리프레시 토큰 생성, 해시값 저장, 원본 토큰 반환
    @Transactional
    public String generate(User user) {
        String rawToken = RefreshTokenGenerator.generate();
        String hash = RefreshTokenGenerator.hash(rawToken);

        RefreshToken refreshToken = RefreshToken.create(user, hash, Duration.ofSeconds(refreshExpireSeconds));

        this.refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    // 리프레시 토큰 유효성 검증 후 새로운 access token 반환
    @Transactional(readOnly = true)
    public String refresh(String rawToken) {

        // 빈 토큰 검사
        if (rawToken.isBlank()) {
            throw new UnauthorizedException(4, "리프레시 토큰이 없습니다");
        }

        String hash = RefreshTokenGenerator.hash(rawToken);

        Optional<RefreshToken> opRefreshToken = this.refreshTokenRepository.findByTokenHash(hash);

        // 유효성 검증
        if(opRefreshToken.isEmpty()) {
            throw new UnauthorizedException(5, "유효하지 않은 토큰입니다.");
        }

        RefreshToken refreshToken = opRefreshToken.get();

        // 폐기된 토큰인지 검증
        if (refreshToken.isRevoked()){
            throw new UnauthorizedException(6, "유효하지 않은 토큰입니다.");
        }

        // 만료된 토큰인지 검증
        if (refreshToken.isExpired()){
            throw new UnauthorizedException(7, "만료된 토큰입니다. 다시 로그인해주세요.");
        }

        return this.userService.genAccessToken(refreshToken.getUser());
    }
}
