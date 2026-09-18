package com.back.nbe12142team06.domain.settlement.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.settlement.client.SettlementClient;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientRequest;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientResponse;
import com.back.nbe12142team06.domain.settlement.dto.AccountDto;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementPersistenceService settlementPersistenceService;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

    // 정산 API
    private final SettlementClient settlementClient;

    @Transactional
    public Settlement request(Long userId, Long settlementId) {

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

        SettlementClientResponse response = settlementApi(settlement.getPayoutAmount(), name, account);

        // 정산 완료 상태 변경
        if (response.res_cnt() < 1) {
            throw new InternalServerErrorException(30, "정산에 실패했습니다.");
        }

        settlement.settlementDone();

        return settlement;
    }

    // 정산 외부 API 로직(목으로 대체)
    @Transactional(readOnly = true)
    public Page<Settlement> findAll(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return settlementRepository.findAllByUserIdAndDate(userId, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Settlement findSettlement(Long userId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new NotFoundException(30, "찾으시는 정산 데이터가 없습니다."));

        if (!settlement.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(30, "정산 요청할 권한이 없습니다.");
        }

        return settlement;
    }

    public int[] settlementProcess() {
        List<AccountDto> accountDtoList = settlementPersistenceService.findAllByStatusAndDate();
        int successCount = 0;
        int failedCount = 0;

        for (AccountDto accountDto : accountDtoList) {
            try {
                SettlementClientResponse response =
                        settlementApi(accountDto.payoutAmount(), accountDto.name(), accountDto.accountNumber());
                // 정산 성공
                if (response.res_cnt() >= 1) {
                    settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.COMPLETED);
                    successCount++;
                    log.info("정산 성공 - settlementId: %s, name: %s, account: %s"
                            .formatted(accountDto.id(), accountDto.name(), accountDto.accountNumber()));
                } else {
                    // 정산 성공 0건
                    settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.FAILED);
                    failedCount++;
                    log.error("정산 실패 - 금융 결제원 API 요청 성공 0건, settlementId: %s, name: %s, account: %s"
                            .formatted(accountDto.id(), accountDto.name(), accountDto.accountNumber()));
                }
            } catch (RuntimeException e) {
                // 금융 결제원 API 요청 에러
                settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.FAILED);
                log.error("정산 실패 - 금융 결제원 API 요청 실패, settlementId: %s, name: %s, account: %s"
                        .formatted(accountDto.id(), accountDto.name(), accountDto.accountNumber()), e);
            }
        }

        return new int[]{successCount + failedCount, successCount, failedCount};
    }

    private SettlementClientResponse settlementApi(int amount, String name, String account) {
        SettlementClientResponse response =
                (SettlementClientResponse) settlementClient.settlementRequest(new SettlementClientRequest(account, name, amount));
        return response;
    }

    @Transactional
    public Settlement createSettlement(int amount, Application application, User escort, LocalDate settledDate) {
        int payoutAmount = (int) (amount * 0.9);
        int platformFee = amount - payoutAmount;

        Settlement settlement = Settlement.builder()
                .payoutAmount(payoutAmount)
                .platformFee(platformFee)
                .settledDate(settledDate)
                .application(application)
                .escort(escort)
                .build();

        return settlementRepository.save(settlement);
    }
}
