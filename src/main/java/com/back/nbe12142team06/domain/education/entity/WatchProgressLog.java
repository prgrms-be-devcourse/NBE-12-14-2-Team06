package com.back.nbe12142team06.domain.education.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "watch_progress_log")
public class WatchProgressLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 교육 진행 상황
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_progress_id", nullable = false)
    private EducationProgress educationProgress;

    // 클라이언트가 보고한 재생 위치
    @Column(nullable = false)
    private double positionSec;

    // 서버가 받은 시각
    @Column(nullable = false)
    private LocalDateTime receivedAt;

    // 정상 시청으로 인정됐는지
    @Column(nullable = false)
    private boolean accepted;

    public WatchProgressLog(EducationProgress educationProgress, double positionSec,
                            LocalDateTime receivedAt, boolean accepted) {
        this.educationProgress = educationProgress;
        this.positionSec = positionSec;
        this.receivedAt = receivedAt;
        this.accepted = accepted;
    }
}