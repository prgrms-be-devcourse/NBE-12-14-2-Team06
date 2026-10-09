package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.payment.client.TossPaymentClient;
import com.back.nbe12142team06.domain.payment.dto.PaymentCancelRequest;
import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.dto.SaveAmountRequest;
import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentPersistenceService paymentPersistenceService;
    private final TossPaymentClient tossPaymentClient;
    private final SettlementService settlementService;

    public Payment confirm(PaymentConfirmRequest request, Long paymentId, Long userId, String sessionAmount, String sessionOrderId) {

        Payment payment = this.findById(userId, paymentId);

        if (payment.getPaymentStatus().equals(PaymentStatus.DONE)) {
            throw new InvalidException(41, "이미 결제를 완료하셨습니다.");
        }

        String tossPaymentKey = request.paymentKey();
        String tossOrderId = request.orderId();
        String amount = request.amount();

        // 1. 검증 로직
        verifyAmount(sessionAmount, sessionOrderId, new SaveAmountRequest(payment.getId(), tossOrderId, amount));
        payment.statusUpdate(PaymentStatus.IN_PROGRESS);
        // 2. 외부 API 호출
        ResponseEntity<TossConfirmResponse> response =
                tossPaymentClient.callApiConfirm(tossPaymentKey, tossOrderId, amount);
        // 3. DB 반영
        try {
            paymentPersistenceService.paymentSaveDb(response, paymentId, tossPaymentKey, tossOrderId);
        } catch (NotFoundException e) {
            cancel(userId, paymentId, new PaymentCancelRequest("서버 에러 발생"));
            log.error("결제 승인 실패", e);
        } catch (RuntimeException ex) {
            cancel(userId, paymentId, new PaymentCancelRequest("서버 에러 발생"));
            log.error("결제 승인 실패", ex);
            throw new InternalServerErrorException(41, "결제 승인 도중 서버 에러가 발생했습니다.");
        }

        log.info("결제 승인 성공 - paymentId: {}, userId: {}", paymentId, userId);

        return payment;
    }

    public List<Payment> findAll(Long userId) {
        return paymentRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Payment findById(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdFetchJoin(paymentId)
                .orElseThrow(() -> new NotFoundException(41, "결제 정보를 찾을 수 없습니다."));

        if (!payment.getPost().getClient().getId().equals(userId)) {
            throw new ForbiddenException(41, "사용자의 결제 정보가 아닙니다.");
        }

        return payment;
    }

    public Payment cancel(Payment payment, PaymentCancelRequest request, int cancelAmount, Boolean postCompleted) {
        String tossPaymentKey = payment.getPaymentKey();
        String cancelReason = request.cancelReason();

        // 외부 API 요청
        ResponseEntity<TossConfirmResponse> response =
                tossPaymentClient.callApiCancel(cancelReason, tossPaymentKey, String.valueOf(cancelAmount));

        // DB 반영
        try {
            if (payment.getBalanceAmount() > cancelAmount || (postCompleted != null && postCompleted)) {
                paymentPersistenceService.paymentPartialCancelDb(payment.getId(), cancelReason, cancelAmount);
            } else {
                paymentPersistenceService.paymentCancelDb(payment.getId(), cancelReason);
            }
        } catch (NotFoundException e) {
            log.error("결제 취소 실패", e);
            throw e;
        } catch (RuntimeException ex) {
            log.error("결제 취소 실패", ex);
            throw new InternalServerErrorException(42, "결제 취소 도중 서버 에러가 발생했습니다.");
        }

        log.info("결제 취소 성공 - paymentId: {}, cancelAmount: {}", payment.getId(), cancelAmount);

        return payment;
    }

    public Payment cancel(Long userId, Long paymentId, PaymentCancelRequest request) {
        Payment payment = findById(userId, paymentId);
        int amount = payment.getAmount();
        return this.cancel(payment, request, amount, null);
    }

    /// deprecated
    public void verifyAmount(Long userId, SaveAmountRequest request) {
        Payment payment = findById(userId, request.paymentId());

        verifyAmount(String.valueOf(payment.getAmount()), request);
    }

    public void verifyAmount(String amount, String orderId, SaveAmountRequest request) {
        if (!amount.equals(request.amount())
        || !orderId.equals(request.orderId())) {
            log.warn("결제 금액 정보 불일치 - 요청 amount: {}, 요청 orderId: {}, 저장된 amount: {}, 저장된 orderId: {}",
                    request.amount(), request.orderId(), amount, orderId);
            throw new InvalidException(42, "결제 금액 정보가 유효하지 않습니다.");
        }
    }

    /// deprecated
    public void verifyAmount(String amount, SaveAmountRequest request) {
        if (amount == null || !amount.equals(request.amount())) {
            log.warn("결제 금액 정보 불일치 - 요청 amount: {}, 저장된 amount: {}", request.amount(), amount);
            throw new InvalidException(42, "결제 금액 정보가 유효하지 않습니다.");
        }
    }

    // userId 삭제 예정
    public void validPayment(Long userId, Post post, Application application, LocalDate settledDate) {
        List<Payment> payments = paymentPersistenceService.findByPostId(post.getId());

        int balanceAmount = 0;
        for (Payment payment : payments) {
            balanceAmount += payment.getAmount();
        }

        balanceAmount -= post.getTotalPay().intValue();
        int payoutAmount = post.getTotalPay().intValue();

        // 추가 결제 플로우
        if (balanceAmount < 0) {
            // 결제 데이터 생성
            Payment newPayment = Payment.builder()
                    .post(post)
                    .hourlyPaySnapshot(post.getHourlyPay())
                    .hours(post.getEscortHours())
                    .amount(Math.abs(balanceAmount))
                    .build();
            paymentPersistenceService.createPayment(newPayment);
        }

        // 부분 취소 플로우
        else if (balanceAmount > 0) {
            // 취소 로직 결제 데이터 생성 없애기
            cancel(
                    payments.getFirst(),
                    new PaymentCancelRequest("결제 금액: %s, 이용 금액: %s".formatted(balanceAmount + payoutAmount, payoutAmount)),
                    balanceAmount, true
            );
        }

        // 정산 데이터 생성
        settlementService.createSettlement(payoutAmount, application, application.getEscort(), settledDate);
    }

    public Payment createPayment(Post post) {
        Payment payment = Payment.builder()
                .post(post)
                .hourlyPaySnapshot(post.getHourlyPay())
                .hours(post.getEscortHours())
                .amount(post.getTotalPay().intValue())
                .build();
        return paymentPersistenceService.createPayment(payment);
    }

    public Payment findByPostIdAndReady(Long postId, Long userId) {
        return paymentRepository.findByPostIdAndUserIdAndReady(postId, userId).orElse(null);
    }

    public void updateAmount(Long paymentId, String amount) {
        paymentPersistenceService.updateAmount(paymentId, amount);
    }

    // 결제 - 공고 삭제 상태 변경
    public Payment cancelPostAndPayment(Long postId) {
        return paymentPersistenceService.updateDeleteStatus(postId);
    }

    public int[] cancelPostAndPaymentCallApi() {
        List<Payment> payments = paymentPersistenceService.findDeletedAll();
        int succeedCount = 0;
        int failedCount = 0;

        for (Payment payment : payments) {
            String tossPaymentKey = payment.getPaymentKey();
            String cancelReason = "공고 삭제로 인한 결제 취소";

            // 외부 API 요청
            try {
                ResponseEntity<TossConfirmResponse> response =
                        tossPaymentClient.callApiCancel(cancelReason, tossPaymentKey, String.valueOf(payment.getBalanceAmount()));
            } catch (RuntimeException e) {
                failedCount++;
                continue;
            }

            // DB 반영
            try {
                paymentPersistenceService.paymentCancelDb(payment.getId(), cancelReason, true);
            } catch (NotFoundException e) {
                log.error("결제 취소 - DB 저장 실패", e);
                failedCount++;
                continue;
            } catch (RuntimeException ex) {
                log.error("결제 취소 - DB 저장 실패", ex);
                failedCount++;
                continue;
            }

            succeedCount++;
            log.info("결제 취소 성공 - paymentId: {}, cancelAmount: {}", payment.getId(), payment.getBalanceAmount());
        }

        return new int[]{succeedCount + failedCount, succeedCount, failedCount};
    }

    public boolean validNotPaid(Long userId) {
        List<Payment> payments = paymentPersistenceService.getNotPaid(userId);

        return payments.isEmpty();
    }
}
