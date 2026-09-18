package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.domain.payment.entity.Payment;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPersistenceService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final RestClient tossRestClient;

    public ResponseEntity<TossConfirmResponse> callApi(String tossPaymentKey, String tossOrderId, String amount) {

        String requestBody = objectMapper.createObjectNode()
                .put("paymentKey", tossPaymentKey)
                .put("orderId", tossOrderId)
                .put("amount", amount)
                .toPrettyString();

        ResponseEntity<TossConfirmResponse> response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .body(requestBody)
                .retrieve()
                .toEntity(TossConfirmResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InvalidException(11, "결제 승인에 실패했습니다.");
        }

        log.info("토스 결제 승인 요청 성공 tossPaymentKey: %s | tossOrderId: %s".formatted(tossPaymentKey, tossOrderId));

        return response;
    }

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
}
