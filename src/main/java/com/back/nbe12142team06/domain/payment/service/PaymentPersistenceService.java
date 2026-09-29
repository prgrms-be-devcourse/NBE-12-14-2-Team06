package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPersistenceService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void paymentSaveDb(ResponseEntity<TossConfirmResponse> response, Long paymentId, String tossPaymentKey, String tossOrderId) {
        TossConfirmResponse body = response.getBody();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(10, "결제 정보를 찾을 수 없습니다."));
        if (body != null) {
            // 승인 시 상태 변경, 더티 체킹으로 자동 변경
            payment.ApprovePayment(tossOrderId, tossPaymentKey, body.method());
        } else {
            payment.ApprovePayment(tossOrderId, tossPaymentKey, null);
        }
    }

    @Transactional
    public Payment paymentCancelDb(Long paymentId, String cancelReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(10, "결제 정보를 찾을 수 없습니다."));
        Payment newPayment = payment.cancelPayment(cancelReason);
        return paymentRepository.save(newPayment);
    }

    @Transactional
    public Payment paymentPartialCancelDb(Long paymentId, String cancelReason, int amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(10, "결제 정보를 찾을 수 없습니다."));
        return payment.cancelPartialPayment(cancelReason, amount);
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> findByPostId(Long postId) {
        return paymentRepository.findByPostId(postId);
    }
}
