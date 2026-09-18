package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.payment.dto.PaymentCancelRequest;
import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.dto.SaveAmountRequest;
import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final RestClient tossRestClient;
    private final PaymentPersistenceService paymentPersistenceService;

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
                paymentPersistenceService.callApi(tossPaymentKey, tossOrderId, amount);
        // 3. DB 반영
        try {
            paymentPersistenceService.paymentSaveDb(response, paymentId, tossPaymentKey, tossOrderId);
        } catch (RuntimeException e) {
            cancel(userId, paymentId, new PaymentCancelRequest("서버 에러 발생"));
            log.error("결제 승인 실패", e);
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

    @Transactional
    public Payment cancel(Long userId, Long paymentId, PaymentCancelRequest request) {
        Payment payment = findById(userId, paymentId);

        // 요청 DTO를 JSON으로 변환
        String requestBody = objectMapper.createObjectNode()
                .put("cancelReason", payment.getPaymentKey())
                .put("cancelAmount", payment.getAmount())
                .toPrettyString();

        ResponseEntity<TossConfirmResponse> response = tossRestClient.post()
                .uri("/v1/payments/%s/cancel".formatted(payment.getPaymentKey()))
                .body(requestBody)
                .retrieve()
                .toEntity(TossConfirmResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InvalidException(12, "결제 취소에 실패했습니다.");
        }

        Payment newPayment = payment.cancelPayment(request.cancelReason());

        paymentRepository.save(newPayment);

        return payment;
    }

    public void verifyAmount(String amount, SaveAmountRequest request) {
        if (amount == null || !amount.equals(request.amount())) {
            throw new InvalidException(10, "결제 금액 정보가 유효하지 않습니다.");
        }
    }
}
