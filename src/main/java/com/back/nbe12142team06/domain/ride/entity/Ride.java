package com.back.nbe12142team06.domain.ride.entity;

import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이동 수단 엔티티
 * 의뢰인 이동 수단 선택
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ride extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 우버 API 요청 번호
    @Column(nullable = false, unique = true)
    private String requestId;

    // 이동 방향
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideDirection direction;

    // 예상 금액
    private int estimatedFare;

    // 실제 금액
    private int actualFare;

    // 요청 시간
    // 지금은 now로 설정했는데 API에서 택시 도착 예정 시간을 보내주면 그거에 맞춰 변경
    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    // 도착 시간
    private LocalDateTime completedAt;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "application_id")
//    @Column(nullable = false)
//    private Application application;
}
