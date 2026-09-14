package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final RestClient tossRestClient;

    @Transactional
    public Payment confirm(PaymentConfirmRequest request, Long postId, Long userId) {

        Optional<Payment> optionalPayment = paymentRepository.findByPostIdAndUserId(postId, userId);
        Payment payment =
                optionalPayment.orElseThrow(() -> new InvalidException(10, "유효하지 않은 회원 정보 또는 공고입니다."));

        payment.statusUpdate(PaymentStatus.IN_PROGRESS);

        String tossPaymentKey = request.paymentKey();
        String tossOrderId = request.orderId();
        String amount = request.amount();

        System.out.println("tossPaymentKey = " + tossPaymentKey);
        System.out.println("tossOrderId = " + tossOrderId);
        System.out.println("amount = " + amount);

        // 요청 DTO를 JSON으로 변환
        String requestBody = objectMapper.createObjectNode()
                .put("paymentKey", tossPaymentKey)
                .put("orderId", tossOrderId)
                .put("amount", amount)
                .toPrettyString();

        ResponseEntity<Void> response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InvalidException(11, "결제 승인에 실패했습니다.");
        }

        // 승인 시 상태 변경, 더티 체킹으로 자동 변경
        payment.ApprovePayment(tossOrderId, tossPaymentKey);

        return payment;
    }

    public List<Payment> findAll(Long userId) {
        return paymentRepository.findAllByUserId(userId);
    }

    public Payment findById(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(10, "결제 정보를 찾을 수 없습니다."));

        if (!payment.getPost().getClient().getId().equals(userId)) {
            throw new InvalidException(10, "사용자의 결제 정보가 아닙니다.");
        }

        return payment;
    }
}
