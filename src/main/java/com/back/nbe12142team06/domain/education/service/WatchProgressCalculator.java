package com.back.nbe12142team06.domain.education.service;

import com.back.nbe12142team06.domain.education.entity.WatchProgressLog;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public final class WatchProgressCalculator {

    // 하트비트 1회당 인정하는 최대 경과 시간
    private static final long MAX_CREDIT_SEC = 15;

    // 네트워크 지연 등을 고려한 허용 배율
    private static final double RATE_TOLERANCE = 1.2;

    // 추가 여유 시간
    private static final double GRACE_SEC = 2;

    // 영상 끝부분 허용 오차
    private static final double END_TOLERANCE_SEC = 1;

    // static인데 생성할까봐 만들었습니다
    private WatchProgressCalculator() {
    }

    public record Result(
            double maxWatchedSec,   // 정상 시청으로 인정된 재생 위치
            boolean completed       // 해당 영상을 전부 시청했는지
    ){}

    public static Result calculate(List<WatchProgressLog> logs, int durationSec){
        double maxWatchedSec = 0;
        LocalDateTime prevReceivedAt = null;

        for (WatchProgressLog log : logs) {
            long credited = creditedSec(prevReceivedAt, log.getReceivedAt());
            double allowedMax = maxWatchedSec + credited *  RATE_TOLERANCE + GRACE_SEC; // 허용 상한선
            double position = log.getPositionSec();

            // 재생 위치가 허용범위 안에 있을 때
            if (position >= 0 && position <= allowedMax) {
                maxWatchedSec = Math.max(maxWatchedSec, position);
            }
            prevReceivedAt = log.getReceivedAt();
        }

        boolean completed = maxWatchedSec >= durationSec - END_TOLERANCE_SEC;

        return new Result(
                maxWatchedSec,
                completed
        );
    }


    // 직전 로그와의 경과 시간을 0 ~ MAX_CREDIT_SEC 범위로 제한
    private static long creditedSec(LocalDateTime prev, LocalDateTime current) {
        if (prev == null) {
            return 0;
        }
        long elapsed = Duration.between(prev, current).toSeconds();
        return Math.min(Math.max(elapsed, 0), MAX_CREDIT_SEC);
    }
}
