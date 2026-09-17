package com.back.nbe12142team06.domain.settlement.service;

import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.settlement.client.SettlementClient;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientRequest;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientResponse;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.user.dto.db.AccountDto;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

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

        String name = settlement.getEscort().getName();
        String account = userRepository.findAccountById(settlement.getEscort().getId());

        SettlementClientResponse response = settlementApi(settlement, name, account);

        // 정산 완료 상태 변경
        if (response.res_cnt() < 1) {
            throw new InternalServerErrorException(30, "정산에 실패했습니다.");
        }

        settlement.settlementDone();

        return settlement;
    }

    // 정산 외부 API 로직(목으로 대체)
    public Page<Settlement> findAll(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return settlementRepository.findAllByUserIdAndDate(userId, startDate, endDate, pageable);
    }

    public Settlement findSettlement(Long userId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new NotFoundException(30, "찾으시는 정산 데이터가 없습니다."));

        if (!settlement.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(30, "정산 요청할 권한이 없습니다.");
        }

        return settlement;
    }

    public int[] settlementProcess() {
        List<Settlement> settlements = settlementRepository.findAllByStatusAndDate();
        int count = 0;

        List<Long> ids = settlements.stream()
                .map(s -> s.getEscort().getId())
                .distinct()
                .toList();
        List<AccountDto> accountByIds = userRepository.findAccountByIds(ids);
        Map<String, String> accountMap = new HashMap<>();

        for (AccountDto accountById : accountByIds) {
            accountMap.put(accountById.name(), accountById.accountNumber());
        }

        for (Settlement settlement : settlements) {
            String name = settlement.getEscort().getName();
            SettlementClientResponse response = settlementApi(settlement, name, accountMap.get(name));

            // 정산 성공
            if (response.res_cnt() >= 1) {
                settlement.settlementDone();
                ++count;
            }
        }

        return new int[]{settlements.size(), count, settlements.size() - count};
    }

    private SettlementClientResponse settlementApi(Settlement settlement, String name, String account) {
        int amount = settlement.getPayoutAmount();
        SettlementClientResponse response =
                (SettlementClientResponse) settlementClient.settlementRequest(new SettlementClientRequest(account, name, amount));
        return response;
    }
}
