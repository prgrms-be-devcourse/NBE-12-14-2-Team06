package com.back.nbe12142team06.domain.settlement.service;

import com.back.nbe12142team06.domain.settlement.dto.AccountDto;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
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
}
