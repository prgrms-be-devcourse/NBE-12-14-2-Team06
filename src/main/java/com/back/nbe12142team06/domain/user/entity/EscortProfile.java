package com.back.nbe12142team06.domain.user.entity;

import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "escort_profiles")
public class EscortProfile extends BaseTimeEntity {

    // 회원 개인 키 ID
    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 신원 인증 여부
    @Column(nullable = false)
    private Boolean verified = false;

    // 신원 인증 날짜, 시각
    private LocalDateTime verifiedAt;

    // 자기소개
    @Column(length = 500)
    private String intro;

    // 동행 완료 건수
    @Column(nullable = false)
    private Integer completedCount = 0;

    // 평점 합계
    @Column(nullable = false)
    private Integer ratingSum = 0;

    // 받은 평점 개수
    @Column(nullable = false)
    private Integer ratingCount = 0;

    // 노쇼 횟수
    @Column(nullable = false)
    private Integer noShowCount = 0;

    // 유저만 연결 생성자
    public EscortProfile(User user){
        this.user = user;
    }
}
