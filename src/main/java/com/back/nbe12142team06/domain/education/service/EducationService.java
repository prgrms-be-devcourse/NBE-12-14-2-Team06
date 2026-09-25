package com.back.nbe12142team06.domain.education.service;

import com.back.nbe12142team06.domain.education.entity.EducationVideo;
import com.back.nbe12142team06.domain.education.entity.WatchProgressLog;
import com.back.nbe12142team06.domain.education.repository.EducationVideoRepository;
import com.back.nbe12142team06.domain.education.repository.WatchProgressLogRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationService {

    // 하트비트 1회당 인정하는 최대 경과 시간
    private static final long MAX_CREDIT_SEC = 15;

    // 네트워크 지연 등을 고려한 허용 배율
    private static final double RATE_TOLERANCE = 1.2;

    // 추가 여유 시간
    private static final double GRACE_SEC = 2;

    // 영상 끝부분 허용 오차
    private static final double END_TOLERANCE_SEC = 1;

    private final EducationVideoRepository educationVideoRepository;
    private final WatchProgressLogRepository watchProgressLogRepository;
    private final EscortProfileRepository escortProfileRepository;
    private final Clock clock;

    @Transactional
    public WatchLogResult recordWatchLog(Long id, Long videoId, Double positionSec) {
        EscortProfile escortProfile = this.escortProfileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("동행 매니저 프로필이 존재하지 않습니다."));

        EducationVideo video = this.educationVideoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("교육 영상이 존재하지 않습니다."));

        LocalDateTime now = LocalDateTime.now(clock);

        // 이미 신원 인증이 완료된 동행인의 경우
        if (escortProfile.getVerified()){
            double maxWatchedSec = maxWatchedSec(findLogs(escortProfile, video));
            return new WatchLogResult(maxWatchedSec, isCompleted(maxWatchedSec, video.getDurationSec()), true);
        }

        // 현재 요청에 대한 로그 생성
        this.watchProgressLogRepository.save(new WatchProgressLog(
                escortProfile,
                video,
                positionSec,
                now
                ));

        double maxWatchedSec = maxWatchedSec(findLogs(escortProfile, video));
        boolean isCompleted = isCompleted(maxWatchedSec, video.getDurationSec());

        // 현재 영상 시청 완료 && 필수 영상 전체 시청 완료 시 프로필 신원 인증 처리
        if (isCompleted && allRequiredCompleted(escortProfile)) {
            escortProfile.verify(now);
        }

        return new WatchLogResult(maxWatchedSec, isCompleted, true);

    }




    private List<WatchProgressLog> findLogs(EscortProfile escortProfile, EducationVideo video) {
        return watchProgressLogRepository
                .findByEscortProfileAndEducationVideoOrderByReceivedAtAscIdAsc(escortProfile, video);
    }

    // 필수 영상 전부 시청 여부
    private boolean allRequiredCompleted(EscortProfile escortProfile) {
        return educationVideoRepository.findAllByRequiredTrue().stream()
                .allMatch(video -> isCompleted(
                        maxWatchedSec(findLogs(escortProfile, video)),
                        video.getDurationSec()
                ));
    }

    // 정상 시청으로 인정된 재생 위치 (logs는 receivedAt 오름차순)
    private static double maxWatchedSec(List<WatchProgressLog> logs) {
        double maxWatchedSec = 0;
        LocalDateTime prevReceivedAt = null;

        for (WatchProgressLog log : logs) {
            long credited = creditedSec(prevReceivedAt, log.getReceivedAt());
            double allowedMax = maxWatchedSec + credited * RATE_TOLERANCE + GRACE_SEC;
            double position = log.getPositionSec();

            if (position >= 0 && position <= allowedMax) {
                maxWatchedSec = Math.max(maxWatchedSec, position);
            }
            prevReceivedAt = log.getReceivedAt();
        }
        return maxWatchedSec;
    }

    // 인정 위치가 영상 끝부분에 도달했는지
    private static boolean isCompleted(double maxWatchedSec, int durationSec) {
        return maxWatchedSec >= durationSec - END_TOLERANCE_SEC;
    }

    private static long creditedSec(LocalDateTime prev, LocalDateTime current) {
        if (prev == null) {
            return 0;
        }
        long elapsed = Duration.between(prev, current).toSeconds();
        return Math.min(Math.max(elapsed, 0), MAX_CREDIT_SEC);
    }
}
