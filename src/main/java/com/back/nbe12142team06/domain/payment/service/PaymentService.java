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
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
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

    public Payment confirm(PaymentConfirmRequest request, Long paymentId, Long userId, String sessionAmount) {

        Payment payment = this.findById(userId, paymentId);
        String tossPaymentKey = request.paymentKey();
        String tossOrderId = request.orderId();
        String amount = request.amount();
        // 1. 검증 로직
        verifyAmount(sessionAmount, new SaveAmountRequest(null, amount));
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
            throw new InternalServerErrorException(10, "결제 승인 도중 서버 에러가 발생했습니다.");
        }

        log.info("결제 승인 성공, %s".formatted(response));

        return payment;
    }

    public List<Payment> findAll(Long userId) {
        return paymentRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Payment findById(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdFetchJoin(paymentId)
                .orElseThrow(() -> new NotFoundException(10, "결제 정보를 찾을 수 없습니다."));

        if (!payment.getPost().getClient().getId().equals(userId)) {
            throw new InvalidException(10, "사용자의 결제 정보가 아닙니다.");
        }

        return payment;
    }

    public Payment cancel(Payment payment, PaymentCancelRequest request, int cancelAmount) {
        String tossPaymentKey = payment.getPaymentKey();
        String cancelReason = request.cancelReason();

        // 외부 API 요청
        ResponseEntity<TossConfirmResponse> response =
                tossPaymentClient.callApiCancel(cancelReason, tossPaymentKey, String.valueOf(cancelAmount));

        // DB 반영
        try {
            if (payment.getBalanceAmount() > cancelAmount) {
                paymentPersistenceService.paymentPartialCancelDb(payment.getId(), cancelReason, cancelAmount);
            } else {
                paymentPersistenceService.paymentCancelDb(payment.getId(), cancelReason);
            }
        } catch (NotFoundException e) {
            log.error("결제 취소 실패", e);
            throw e;
        } catch (RuntimeException ex) {
            log.error("결제 취소 실패", ex);
            throw new InternalServerErrorException(11, "결제 취소 도중 서버 에러가 발생했습니다.");
        }

        log.info("결제 취소 성공, %s".formatted(response));

        return payment;
    }

    public Payment cancel(Long userId, Long paymentId, PaymentCancelRequest request) {
        Payment payment = findById(userId, paymentId);
        int amount = payment.getAmount();
        return this.cancel(payment, request, amount);
    }

    public void verifyAmount(String amount, SaveAmountRequest request) {
        if (amount == null || !amount.equals(request.amount())) {
            throw new InvalidException(10, "결제 금액 정보가 유효하지 않습니다.");
        }
    }

    // userId 삭제 예정
    public void validPayment(Long userId, Post post, Application application, LocalDate settledDate) {
        Payment payment = paymentPersistenceService.findByPostId(post.getId());
        int balanceAmount = payment.getAmount() - post.getTotalPay().intValue();
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
                    payment,
                    new PaymentCancelRequest("결제 금액: %s, 이용 금액: %s".formatted(payment.getAmount(), post.getTotalPay().intValue())),
                    balanceAmount
            );
        }

        // 정산 데이터 생성
        settlementService.createSettlement(payoutAmount, application, application.getEscort(), settledDate);

        // 사용자에게 결제 요청
    }

    public void createPayment(Post post) {
        Payment payment = Payment.builder()
                .post(post)
                .hourlyPaySnapshot(post.getHourlyPay())
                .hours(post.getEscortHours())
                .amount(post.getTotalPay().intValue())
                .build();
        paymentPersistenceService.createPayment(payment);
    }
}
