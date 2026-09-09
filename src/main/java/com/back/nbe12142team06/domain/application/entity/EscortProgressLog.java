package com.back.nbe12142team06.domain.application.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EscortProgressLog {
    // 진행 이력 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 해당 지원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    // 동행 진행 단계
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscortProgress progress = EscortProgress.NOT_STARTED;

    // 해당 단계 진입 시각
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}