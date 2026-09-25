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
    @Column(nullable = false, length = 500)
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

    // 은행 이름
    @Column(nullable = false, length = 20)
    private String bankName;

    // 예금주명
    @Column(nullable = false, length = 50)
    private String accountHolder;

    // 계좌번호
    @Column(nullable = false, length = 30)
    private String accountNumber;

    public EscortProfile(User user, String intro, String bankName, String accountHolder, String accountNumber){
        this.user = user;
        this.intro = intro;
        this.bankName = bankName;
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
    }

    // 프로필 업데이트
    public void updateProfile(String intro, String bankName, String accountHolder, String accountNumber){
        this.intro = intro;
        this.bankName = bankName;
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
    }

    // 리뷰 평점 반영
    public void addRating(int rating) {
        this.ratingSum += rating;
        this.ratingCount++;
    }

    // 평균 평점 (소수점 첫째 자리, 평가 없으면 null)
    public Double getAverageRating() {
        if (this.ratingCount == 0) {
            return null;
        }
        return Math.round(this.ratingSum * 10.0 / this.ratingCount) / 10.0;
    }

    // 동행 완료 건수 반영
    public void increaseCompletedCount() {
        this.completedCount++;
    }

    // 노쇼 횟수 증가
    public void increaseNoShowCount() {
        this.noShowCount++;
    }

    // 교육 이수 처리
    public void verify(LocalDateTime now) {
        if (this.verified) {
            return;
        }
        this.verifiedAt = now;
        this.verified = true;
    }

}
