package com.back.nbe12142team06.domain.education.entity;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "education_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_education_progress_escort_video",
                columnNames = {"escort_profile_id", "education_video_id"}
        )
)
public class EducationProgress {

    // 하트비트 1회당 인정하는 최대 경과 시간
    private static final long MAX_CREDIT_SEC = 15;

    // 네트워크 지연 등을 고려한 허용 배율
    private static final double RATE_TOLERANCE = 1.2;

    // 추가 여유 시간
    private static final double GRACE_SEC = 2;

    // 영상 끝부분 허용 오차
    private static final double END_TOLERANCE_SEC = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escort_profile_id", nullable = false)
    private EscortProfile escortProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_video_id", nullable = false)
    private EducationVideo educationVideo;

    // 정상 시청으로 인정된 재생 위치
    @Column(nullable = false)
    private double maxWatchedSec;

    // 마지막으로 재생 위치를 받은 시각 (아직 없으면 null)
    private LocalDateTime lastReceivedAt;

    // 영상 시청 완료 여부
    @Column(nullable = false)
    private boolean completed;

    // 영상 시청 완료 시각
    private LocalDateTime completedAt;

    public EducationProgress(EscortProfile escortProfile, EducationVideo educationVideo) {
        this.escortProfile = escortProfile;
        this.educationVideo = educationVideo;
        this.maxWatchedSec = 0;
        this.completed = false;
    }

    // 재생 위치를 반영하고 인정 여부를 반환
    public boolean record(double positionSec, LocalDateTime now) {
        double allowedMax = maxWatchedSec + creditedSec(now) * RATE_TOLERANCE + GRACE_SEC;
        this.lastReceivedAt = now;


        if (!(positionSec >= 0 && positionSec <= allowedMax)) {
            return false;
        }

        this.maxWatchedSec = Math.max(maxWatchedSec, positionSec);

        if (!completed && maxWatchedSec >= educationVideo.getDurationSec() - END_TOLERANCE_SEC) {
            this.completed = true;
            this.completedAt = now;
        }
        return true;
    }

    // 직전 수신 이후 경과 시간을 0 ~ MAX_CREDIT_SEC 범위로 제한
    private long creditedSec(LocalDateTime now) {
        if (lastReceivedAt == null) {
            return 0;
        }
        long elapsed = Duration.between(lastReceivedAt, now).toSeconds();
        return Math.min(Math.max(elapsed, 0), MAX_CREDIT_SEC);
    }
}