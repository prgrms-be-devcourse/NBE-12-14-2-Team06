package com.back.nbe12142team06.domain.auth;

import com.back.nbe12142team06.domain.auth.entity.RefreshToken;
import com.back.nbe12142team06.domain.auth.repository.RefreshTokenRepository;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.security.RefreshTokenGenerator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RefreshTokenTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager em;

    private User createUser() {
        return this.userService.signUp(
                new UserSignUpRequest(
                        "user1",
                        "1234",
                        "user@test.test",
                        "유저1",
                        Role.CLIENT,
                        Gender.MALE,
                        LocalDate.of(1990, 5, 6),
                        "010-1234-5678",
                        "서울"
                )
        );
    }


    @Test
    @DisplayName("[RefreshTokenGenerator] 해시 검증")
    void t1() {
        String raw = RefreshTokenGenerator.generate();

        assertThat(RefreshTokenGenerator.hash(raw))
                .isEqualTo(RefreshTokenGenerator.hash(raw));
    }

    @Test
    @DisplayName("[RefreshTokenGenerator] 서로 다른 난수값 생성 검증")
    void t2() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            tokens.add(RefreshTokenGenerator.generate());
        }

        assertThat(tokens).hasSize(1000);
    }

    @Test
    @DisplayName("[RefreshTokenGenerator] 해시값 길이 검증")
    void t3() {
        assertThat(RefreshTokenGenerator.hash("anything")).hasSize(64);
    }

    @Test
    @DisplayName("[RefreshToken] 저장 후 해시로 조회")
    void t4() {
        User user = createUser();
        String hash = RefreshTokenGenerator.hash(RefreshTokenGenerator.generate());

        this.refreshTokenRepository.save(RefreshToken.create(user, hash, Duration.ofDays(14)));

        em.flush();
        em.clear();

        assertThat(refreshTokenRepository.findByTokenHash(hash)).isPresent();
    }

    @Test
    @DisplayName("[RefreshToken] 중복 해시 저장 시 유니크 예외 발생")
    void t5() {
        User user = createUser();
        String hash = RefreshTokenGenerator.hash("same");
        refreshTokenRepository.save(RefreshToken.create(user, hash, Duration.ofDays(14)));

        // 같은 해시값으로 저장 시도
        assertThatThrownBy(() -> refreshTokenRepository.save(RefreshToken.create(user, hash, Duration.ofDays(14))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("[RefreshToken] 폐기,만료 상태 판정 검증")
    void t6() {
        User user = createUser();

        // 유효 토큰
        RefreshToken live = RefreshToken.create(user, "hash-live", Duration.ofDays(14));
        // 기간 만료 토큰
        RefreshToken expired = RefreshToken.create(user, "hash-expired", Duration.ofSeconds(-1));
        // 폐기 토큰
        RefreshToken revoked = RefreshToken.create(user, "hash-revoked", Duration.ofDays(14));

        // 토큰 폐기
        revoked.revoke();
        // 첫 폐기 시점
        LocalDateTime firstRevokedAt = revoked.getRevokedAt();
        // 토큰 재폐기
        revoked.revoke();

        assertAll(
                () -> assertThat(live.isUsable()).isTrue(),
                () -> assertThat(expired.isExpired()).isTrue(),
                () -> assertThat(revoked.isRevoked()).isTrue(),
                () -> assertThat(revoked.getRevokedAt()).isEqualTo(firstRevokedAt)
        );
    }
}
