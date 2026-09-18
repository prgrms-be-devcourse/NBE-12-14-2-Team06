package com.back.nbe12142team06.domain.payment.client;

import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.rmi.ServerException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TossPaymentClient {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public ResponseEntity<TossConfirmResponse> callApiConfirm(String tossPaymentKey, String tossOrderId, String amount) {

        String requestBody = objectMapper.createObjectNode()
                .put("paymentKey", tossPaymentKey)
                .put("orderId", tossOrderId)
                .put("amount", amount)
                .toPrettyString();

        ResponseEntity<TossConfirmResponse> response;
        try {
            response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .body(requestBody)
                    .retrieve()
                    .toEntity(TossConfirmResponse.class);
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(13, e.getMessage());
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InvalidException(11, "결제 승인에 실패했습니다.");
        }

        log.info("토스 결제 승인 요청 성공 tossPaymentKey: %s | tossOrderId: %s | amount: %s".formatted(tossPaymentKey, tossOrderId, amount));

        return response;
    }

    public ResponseEntity<TossConfirmResponse> callApiCancel(String cancelReason, String tossPaymentKey, String amount) {

        // 요청 DTO를 JSON으로 변환
        String requestBody = objectMapper.createObjectNode()
                .put("cancelReason", cancelReason)
                .put("cancelAmount", amount)
                .toPrettyString();

        ResponseEntity<TossConfirmResponse> response;
        try {
            response = tossRestClient.post()
                    .uri("/v1/payments/%s/cancel".formatted(tossPaymentKey))
                    .body(requestBody)
                    .retrieve()
                    .toEntity(TossConfirmResponse.class);
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(13, e.getMessage());
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InvalidException(12, "결제 취소에 실패했습니다.");
        }


        log.info("토스 결제 취소 요청 성공 tossPaymentKey: %s | cancelReason: %s | amount: %s".formatted(tossPaymentKey, cancelReason, amount));

        return response;
    }
}
