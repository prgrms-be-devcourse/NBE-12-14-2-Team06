package com.back.nbe12142team06.domain.settlement.scheduler;

import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final SettlementService settlementService;

    // 평일 10시마다 실행
    // 정산 실패했던 건에 대해서도 재시도
    @Scheduled(cron = "0 0 10 * * 1-5")
    public void expireOverduePosts() {
        int[] counts = settlementService.settlementProcess();

        // 로그
        log.info("정산 스케줄링 총 %d건, 성공 %d건, 실패 %d건".formatted(counts[0], counts[1], counts[2]));
    }
}
