package com.back.nbe12142team06.domain.education.service;

import com.back.nbe12142team06.domain.education.entity.WatchProgressLog;
import com.back.nbe12142team06.domain.education.service.WatchProgressCalculator.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class WatchProgressCalculatorTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 9, 1, 10, 0, 0);
    private static final int DURATION_SEC = 60;

    // 계산기는 위치와 수신 시각만 사용하므로 프로필과 영상은 null로 둔다
    private WatchProgressLog log(double positionSec, long receivedAfterSec) {
        return new WatchProgressLog(null, null, positionSec, BASE.plusSeconds(receivedAfterSec));
    }

    // 0초부터 untilSec까지 10초 간격으로 정상 재생한 로그
    private List<WatchProgressLog> normalLogsUntil(int untilSec) {
        List<WatchProgressLog> logs = new ArrayList<>();
        for (int sec = 0; sec <= untilSec; sec += 10) {
            logs.add(log(sec, sec));
        }
        return logs;
    }

    @Test
    @DisplayName("시청 기록이 없으면 완료 불가")
    void t1() {
        Result result = WatchProgressCalculator.calculate(List.of(), DURATION_SEC);

        assertThat(result.maxWatchedSec()).isEqualTo(0);
        assertThat(result.completed()).isFalse();
    }

    @Test
    @DisplayName("10초 간격으로 정상 재생해 끝까지 보면 완료된다")
    void t2() {
        List<WatchProgressLog> logs = normalLogsUntil(50);
        logs.add(log(60.1, 60)); // 재생 종료 시점 보고

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.maxWatchedSec()).isCloseTo(60.1, within(0.001));
        assertThat(result.completed()).isTrue();
    }

    @Test
    @DisplayName("중간까지만 보면 완료되지 않는다")
    void t3() {
        Result result = WatchProgressCalculator.calculate(normalLogsUntil(30), DURATION_SEC);

        assertThat(result.maxWatchedSec()).isCloseTo(30, within(0.001));
        assertThat(result.completed()).isFalse();
    }

    @Test
    @DisplayName("앞으로 건너뛴 위치는 인정되지 않는다")
    void t4() {
        List<WatchProgressLog> logs = List.of(
                log(0, 0),
                log(10, 10),
                log(40, 20)  // 허용치 10 + 10*1.2 + 2 = 24초 초과
        );

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.maxWatchedSec()).isCloseTo(10, within(0.001));
    }

    @Test
    @DisplayName("2배속으로 재생하면 인정 위치가 올라가지 않는다")
    void t5() {
        List<WatchProgressLog> logs = List.of(
                log(0, 0),
                log(20, 10),
                log(40, 20),
                log(60, 30)
        );

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.maxWatchedSec()).isEqualTo(0);
        assertThat(result.completed()).isFalse();
    }

    @Test
    @DisplayName("오래 멈춘 뒤 건너뛰어도 최대 15초까지만 경과 시간으로 인정된다")
    void t6() {
        List<WatchProgressLog> logs = List.of(
                log(0, 0),
                log(10, 10),
                log(40, 310)  // 5분 경과했지만 15초만 인정 → 허용치 10 + 15*1.2 + 2 = 30초
        );

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.maxWatchedSec()).isCloseTo(10, within(0.001));
    }

    @Test
    @DisplayName("오래 멈춘 뒤 이어서 정상 재생하면 인정된다")
    void t7() {
        List<WatchProgressLog> logs = List.of(
                log(0, 0),
                log(10, 10),
                log(10, 310),
                log(20, 320)
        );

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.maxWatchedSec()).isCloseTo(20, within(0.001));
    }

    @Test
    @DisplayName("뒤로 감아도 이미 인정된 위치는 줄어들지 않는다")
    void t8() {
        List<WatchProgressLog> logs = normalLogsUntil(20);
        logs.add(log(5, 30));

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.maxWatchedSec()).isCloseTo(20, within(0.001));
    }

    @Test
    @DisplayName("음수 위치는 인정되지 않는다")
    void t9() {
        List<WatchProgressLog> logs = List.of(log(-1, 0));

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.maxWatchedSec()).isEqualTo(0);
    }

    @Test
    @DisplayName("거부된 로그도 다음 판정의 시간 기준점이 된다")
    void t10() {
        List<WatchProgressLog> logs = List.of(
                log(0, 0),
                log(30, 12),  // 허용치 0 + 12*1.2 + 2 = 16.4초 초과 → 거부
                log(20, 24)   // 기준점이 12초면 허용치 16.4초 → 거부
                // (기준점이 0초로 남았다면 15초 인정 → 허용치 20초 → 인정되어 버림)
        );

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.maxWatchedSec()).isEqualTo(0);
    }

    @Test
    @DisplayName("영상 끝에서 1초 이내로 못 미쳐도 완료된다")
    void t11() {
        List<WatchProgressLog> logs = normalLogsUntil(50);
        logs.add(log(59.2, 60));

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.completed()).isTrue();
    }

    @Test
    @DisplayName("영상 끝에서 1초 넘게 못 미치면 완료되지 않는다")
    void t12() {
        List<WatchProgressLog> logs = normalLogsUntil(50);
        logs.add(log(58.9, 60));

        Result result = WatchProgressCalculator.calculate(logs, DURATION_SEC);

        assertThat(result.completed()).isFalse();
    }
}