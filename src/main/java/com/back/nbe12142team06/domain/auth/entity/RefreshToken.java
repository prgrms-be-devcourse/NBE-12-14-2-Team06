package com.back.nbe12142team06.domain.auth.entity;

import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 이미 revoked_at이 null이 아닌 토큰이 날아오면 탈취 감지 가능하다합니다!
    private LocalDateTime revokedAt;

    private RefreshToken(User user, String tokenHash, LocalDateTime expiryAt){
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiryAt;
    }

    // now + ttl(Time to Live)를 만료 시각으로 설정
    public static RefreshToken issue(User user, String tokenHash, Duration ttl){
        return new RefreshToken(user, tokenHash, LocalDateTime.now().plus(ttl));
    }

    // 토큰 만료 설정
    public void revoke() {
        if (this.revokedAt == null) {
            this.revokedAt = LocalDateTime.now();
        }
    }

    // 이미 만료된 토큰인지 확인
    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    // 토큰의 생존 시간이 만료되었는지 확인
    public boolean isExpired() {
        return LocalDateTime.now().isBefore(LocalDateTime.now());
    }

    // 사용 가능한 토큰인지 확인
    public boolean isUsable() {
        return !this.isRevoked() && !this.isExpired();
    }



}
