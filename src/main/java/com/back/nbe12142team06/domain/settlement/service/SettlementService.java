package com.back.nbe12142team06.domain.settlement.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.penalty.entity.NoShowPenalty;
import com.back.nbe12142team06.domain.penalty.service.NoShowPenaltyService;
import com.back.nbe12142team06.domain.settlement.client.SettlementClient;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientRequest;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientResponse;
import com.back.nbe12142team06.domain.settlement.dto.AccountDto;
import com.back.nbe12142team06.domain.settlement.dto.SettlementResponse;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.user.entity.User;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementPersistenceService settlementPersistenceService;
    private final SettlementRepository settlementRepository;
    private final NoShowPenaltyService noShowPenaltyService;

    // 정산 API
    private final SettlementClient settlementClient;

    // 단일 정산 요청
    public void request(Long userId, Long settlementId) {

        AccountDto accountDto = settlementPersistenceService.findAccountDto(userId, settlementId);

        String name = accountDto.name();
        String account = accountDto.accountNumber();

        // 정산 중 상태 변경
        int row = settlementPersistenceService.processingSettlement(settlementId);

        // 변경 된 행이 0개라면 정산 중이거나 정산이 완료 된 경우
        if (row == 0) {
            throw new InvalidException(71, "이미 정산 중이거나 정산이 완료되었습니다.");
        }

        try {
            SettlementClientResponse response = settlementApi(accountDto.payoutAmount(), name, account);
            // 정산 완료 상태 변경
            if (response.res_cnt() >= 1) {
                try {
                    settlementPersistenceService.updateSettlement(settlementId, SettlementStatus.COMPLETED);
                    log.info("정산 성공 - settlementId: {}", accountDto.id());
                } catch (Exception e) {
                    log.error("정산 DB 변경 실패, 정산은 성공 - settlementId: {}", accountDto.id());
                    throw new InternalServerErrorException(71, "정산 데이터 저장 중 DB 에러가 발생했습니다.");
                }
            } else {
                settlementPersistenceService.updateSettlement(settlementId, SettlementStatus.FAILED);
                log.error("정산 실패 - 금융 결제원 API 요청 실패, settlementId: {}", settlementId);
                throw new InternalServerErrorException(72, "정산에 실패했습니다.");
            }
        } catch (RuntimeException e) {
            // 금융 결제원 API 요청 에러
            settlementPersistenceService.updateSettlement(settlementId, SettlementStatus.FAILED);
            log.error("정산 실패 - 금융 결제원 API 요청 실패, settlementId: {}", settlementId, e);
            throw new InternalServerErrorException(72, "정산에 실패했습니다.");
        }
    }

    // 정산 외부 API 로직(목으로 대체)
    @Transactional(readOnly = true)
    public Page<SettlementResponse> findAll(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return settlementRepository.findAllByUserIdAndDate(userId, startDate, endDate, pageable).map(SettlementResponse::new);
    }

    @Transactional(readOnly = true)
    public SettlementResponse findSettlement(Long userId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new NotFoundException(71, "정산 데이터를 찾을 수 없습니다."));

        if (!settlement.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(71, "정산 요청할 권한이 없습니다.");
        }

        return new SettlementResponse(settlement);
    }

    // 정산 스케줄링
    public int[] settlementProcess() {
        List<AccountDto> accountDtoList = settlementPersistenceService.findAllByStatusAndDate();
        int successCount = 0;
        int failedCount = 0;

        for (AccountDto accountDto : accountDtoList) {
            // 정산 중 상태 변경
            int row = settlementPersistenceService.processingSettlement(accountDto.id());

            // 변경 된 행이 0개라면 정산 중이거나 정산이 완료 된 경우
            if (row == 0) {
                continue;
            }

            try {
                SettlementClientResponse response =
                        settlementApi(accountDto.payoutAmount(), accountDto.name(), accountDto.accountNumber());
                // 정산 성공
                if (response.res_cnt() >= 1) {
                    settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.COMPLETED);
                    successCount++;
                    log.info("정산 성공 - settlementId: {}", accountDto.id());
                } else {
                    // 정산 성공 0건
                    settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.FAILED);
                    failedCount++;
                    log.error("정산 실패 - 금융 결제원 API 요청 성공 0건, settlementId: {}", accountDto.id());
                }
            } catch (RuntimeException e) {
                // 금융 결제원 API 요청 에러
                settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.FAILED);
                log.error("정산 실패 - 금융 결제원 API 요청 실패, settlementId: {}", accountDto.id(), e);
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
    public Settlement createSettlement(int payoutAmount, Application application, User escort, LocalDate settledDate) {
        int settlementAmount = (int) (payoutAmount * 0.9);
        int platformFee = payoutAmount - settlementAmount;

        // 패널티 적용해야 하는지 확인
        Optional<NoShowPenalty> noShowPenalty = noShowPenaltyService.getNoShowPenalty(escort.getId());

        // 패널티 적용 시 정산 금액 차감
        int penaltyAmount = 0;
        if (noShowPenalty.isPresent()) {
            penaltyAmount = (int) (settlementAmount * 0.1);
            settlementAmount -= penaltyAmount;
        }

        Settlement settlement = Settlement.builder()
                .payoutAmount(settlementAmount)
                .platformFee(platformFee)
                .penaltyAmount(penaltyAmount)
                .settledDate(settledDate)
                .application(application)
                .escort(escort)
                .build();

        Settlement savedSettlement = settlementRepository.save(settlement);

        // 패널티 데이터 업데이트
        if (noShowPenalty.isPresent()) {
            noShowPenaltyService.applyPenalty(escort.getId(), penaltyAmount,
                    settlementAmount + penaltyAmount, settlement, noShowPenalty.get());
        }

        return savedSettlement;
    }
}
