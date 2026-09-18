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

    public Settlement findSettlement(Long userId, Long settlementId) {
        // 정산 데이터 조회
        Settlement settlement = settlementRepository.findByIdAndState(settlementId)
                .orElseThrow(() -> new NotFoundException(30, "찾으시는 정산 데이터가 없습니다."));

        if (!settlement.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(30, "정산 요청할 권한이 없습니다.");
        }

        if (!settlement.getApplication().getPost().getPostStatus().equals(PostStatus.COMPLETED)) {
            throw new InvalidException(30, "아직 완료되지 않은 동행 의뢰입니다.");
        }
        return settlement;
    }
}
