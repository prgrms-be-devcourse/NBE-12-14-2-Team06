package com.back.nbe12142team06.domain.settlement.service;

import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.settlement.dto.AccountDto;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementPersistenceService {

    private final SettlementRepository settlementRepository;

    public List<AccountDto> findAllByStatusAndDate() {
        return settlementRepository.findAllByStatusAndDate();
    }

    @Transactional
    public int updateSettlement(Long settlementId, SettlementStatus status) {
        return settlementRepository.updateStatus(settlementId, status);
    }

    public AccountDto findAccountDto(Long userId, Long settlementId) {
        // 정산 데이터 조회
        return settlementRepository.findByIdAndState(userId, settlementId)
                .orElseThrow(() -> new NotFoundException(71, "정산 데이터를 찾을 수 없습니다."));
    }

    @Transactional
    public int processingSettlement(Long settlementId) {
        return settlementRepository.updateProcessing(settlementId);
    }
}
