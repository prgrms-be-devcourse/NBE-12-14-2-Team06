package com.back.nbe12142team06.domain.education.entity;


import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "watch_log")
public class WatchProgressLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 동행인 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escort_profile_id", nullable = false)
    private EscortProfile escortProfile;

    // 영상 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_video_id", nullable = false)
    private EducationVideo educationVideo;

    // 클라이언트가 보고한 재생 위치
    @Column(nullable = false)
    private double positionSec;

    // 서버가 받은 시각
    @Column(nullable = false)
    private LocalDateTime receivedAt;


    public WatchProgressLog(EscortProfile escortProfile, EducationVideo educationVideo,
                    double positionSec, LocalDateTime receivedAt) {
        this.escortProfile = escortProfile;
        this.educationVideo = educationVideo;
        this.positionSec = positionSec;
        this.receivedAt = receivedAt;
    }
}
