package com.back.nbe12142team06.domain.settlement.service;

import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.settlement.client.SettlementClient;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientRequest;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientResponse;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementRepository settlementRepository;

    // 정산 API
    private final SettlementClient settlementClient;

    @Transactional
    public Settlement request(Long userId, Long settlementId) {

        // 정산 데이터 생성, 지원 승인 및 매칭 확정이 되어야 정산 데이터 생성 가능

        // 정산 데이터 조회
        Settlement settlement = settlementRepository.findByIdAndState(settlementId)
                .orElseThrow(() -> new NotFoundException(30, "찾으시는 정산 데이터가 없습니다."));

        if (!settlement.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(30, "정산 요청할 권한이 없습니다.");
        }

        if (!settlement.getApplication().getPost().getPostStatus().equals(PostStatus.COMPLETED)) {
            throw new InvalidException(30, "아직 완료되지 않은 동행 의뢰입니다.");
        }

        // 정산 외부 API 로직(목으로 대체)

        User escort = settlement.getEscort();
//        String account = escort.getAccount();
        String account = "계좌 필드 생기면 위에껄로 대체";
        String name = escort.getName();
        int amount = settlement.getPayoutAmount();
        SettlementClientResponse response =
                (SettlementClientResponse) settlementClient.settlementRequest(new SettlementClientRequest(account, name, amount));

        // 정산 완료 상태 변경
        if (response.res_cnt() < 1) {
            throw new InternalServerErrorException(30, "정산에 실패했습니다.");
        }

        settlement.settlementDone();

        return settlement;
    }

    public List<Settlement> findAll(Long userId, LocalDate startDate, LocalDate endDate) {
        return settlementRepository.findAllByUserIdAndDate(userId, startDate, endDate);
    }
}
