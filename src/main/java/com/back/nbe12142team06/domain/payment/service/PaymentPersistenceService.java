package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentPersistenceService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void paymentSaveDb(ResponseEntity<TossConfirmResponse> response, Long paymentId, String tossPaymentKey, String tossOrderId) {
        TossConfirmResponse body = response.getBody();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(41, "결제 정보를 찾을 수 없습니다."));
        if (body != null) {
            // 승인 시 상태 변경, 더티 체킹으로 자동 변경
            payment.ApprovePayment(tossOrderId, tossPaymentKey, body.method());
        } else {
            payment.ApprovePayment(tossOrderId, tossPaymentKey, null);
        }
    }

    @Transactional
    public Payment paymentCancelDb(Long paymentId, String cancelReason) {
        Payment newPayment = paymentCancelDb(paymentId, cancelReason, false);
        return paymentRepository.save(newPayment);
    }

    @Transactional
    public Payment paymentCancelDb(Long paymentId, String cancelReason, boolean postDeleted) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(41, "결제 정보를 찾을 수 없습니다."));
        return payment.cancelPayment(cancelReason);
    }

    @Transactional
    public Payment paymentPartialCancelDb(Long paymentId, String cancelReason, int amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(41, "결제 정보를 찾을 수 없습니다."));
        return payment.cancelPartialPayment(cancelReason, amount);
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<Payment> findByPostId(Long postId) {
        return paymentRepository.findSuccessPayByPostId(postId);
    }

    public Payment findById(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdFetchJoin(paymentId)
                .orElseThrow(() -> new NotFoundException(41, "결제 정보를 찾을 수 없습니다."));

        if (!payment.getPost().getClient().getId().equals(userId)) {
            throw new ForbiddenException(41, "사용자의 결제 정보가 아닙니다.");
        }

        return payment;
    }

    @Transactional
    public Payment updateAmount(Long paymentId, String amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(41, "결제 정보를 찾을 수 없습니다."));
        payment.updateAmount(Integer.parseInt(amount));
        return payment;
    }

    // 공고 삭제 트랜잭션이랑 다른 트랜잭션으로 진행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment updateDeleteStatus(Long postId) {
        Payment payment = paymentRepository.findByPostId(postId)
                .orElseThrow(() -> new NotFoundException("결제 정보를 찾을 수 없습니다."));
        payment.statusUpdate(PaymentStatus.DELETED);
        payment.updateCanceledAt();
        return payment;
    }

    public List<Payment> findDeletedAll() {
        return paymentRepository.findDeletedAll();
    }

    public List<Payment> getNotPaid(Long userId) {
        return paymentRepository.findNotPaidByUserId(userId);
    }

    @Transactional
    public int confirmUpdateStatus(Long paymentId) {
        return paymentRepository.paymentInProgress(paymentId);
    }
}
