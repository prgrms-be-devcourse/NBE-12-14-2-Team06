package com.back.nbe12142team06.domain.penalty.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.penalty.entity.NoShowPenalty;
import com.back.nbe12142team06.domain.penalty.repository.NoShowPenaltyRepository;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoShowPenaltyService {

    private final NoShowPenaltyRepository noShowPenaltyRepository;

    @Transactional
    public void noShow(Application application) {
        // 패널티 데이터 생성
        NoShowPenalty noShowPenalty = NoShowPenalty.builder()
                .application(application)
                .build();

        noShowPenaltyRepository.save(noShowPenalty);
        log.info("노쇼 데이터 생성 - NoShowPenaltyId: %s".formatted(noShowPenalty.getId()));
    }

    @Transactional
    public void applyPenalty(Long escortId, int penaltyAmount, int baseAmount, Settlement settlement, NoShowPenalty noShowPenalty) {
        noShowPenalty.updateNoShow(penaltyAmount, baseAmount, settlement);
        log.info("노쇼 패널티 적용 완료 - escortId: %s, settlementId: %s".formatted(escortId, settlement.getId()));
    }

    public Optional<NoShowPenalty> getNoShowPenalty(Long escortId) {
        return noShowPenaltyRepository.findByEscortIdAndStatus(escortId).stream().findFirst();
    }
}
