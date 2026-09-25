package com.back.nbe12142team06.domain.ride.entity;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이동 수단 엔티티
 * 의뢰인 이동 수단 선택
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ride extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이동 방향
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RideDirection direction = RideDirection.TO_HOSPITAL;

    // 이동 수단 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RideStatus rideStatus = RideStatus.ACCEPTED;

    @Enumerated(EnumType.STRING)
    private RideSelect selected;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // 이동수단 선택 시 정보 업데이트
    public void rideUpdate(String selected) {
        updateSelect(RideSelect.valueOf(selected));
    }

    public void updateSelect(RideSelect selected) {
        this.selected = selected;
    }

    // 이동 상태 변경
    public void updateStatus(RideStatus rideStatus) {
        this.rideStatus = rideStatus;
    }
}
