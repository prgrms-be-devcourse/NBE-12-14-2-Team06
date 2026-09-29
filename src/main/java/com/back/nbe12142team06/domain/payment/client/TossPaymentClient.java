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

    /**
     * BaseInitData 가 만드는 시드 결제 키의 접두어. 이 키는 실제 토스 서버에 존재하지 않아서
     * 진짜 API 를 부르면 항상 404(NOT_FOUND_PAYMENT)가 난다 — 반복 테스트를 위해, 이 접두어로
     * 시작하는 키는 실제 토스 API 를 호출하지 않고 성공한 것처럼 처리한다. 진짜 결제 키(실제
     * 토스 결제창을 거쳐 발급된 키)는 이 접두어를 쓰지 않으므로 정상적으로 진짜 API 를 탄다.
     */
    private static final String SEED_PAYMENT_KEY_PREFIX = "INIT-PAYKEY-";

    private boolean isSeedPaymentKey(String tossPaymentKey) {
        return tossPaymentKey != null && tossPaymentKey.startsWith(SEED_PAYMENT_KEY_PREFIX);
    }

    public ResponseEntity<TossConfirmResponse> callApiConfirm(String tossPaymentKey, String tossOrderId, String amount) {

        if (isSeedPaymentKey(tossPaymentKey)) {
            log.info("[시드 결제] 실제 토스 API 호출 없이 승인 처리 - tossOrderId: {}, amount: {}", tossOrderId, amount);
            return ResponseEntity.ok(new TossConfirmResponse("카드", amount));
        }

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

        // [로그 정리] tossPaymentKey(결제 조회·취소에 쓰는 키)가 로그에 남아서 주석 처리하고, 아래에서는 키를 뺀 정보만 남김
        // log.info("토스 결제 승인 요청 성공 tossPaymentKey: %s | tossOrderId: %s | amount: %s".formatted(tossPaymentKey, tossOrderId, amount));
        log.info("토스 결제 승인 요청 성공 - tossOrderId: {}, amount: {}", tossOrderId, amount);

        return response;
    }

    public ResponseEntity<TossConfirmResponse> callApiCancel(String cancelReason, String tossPaymentKey, String amount) {

        if (isSeedPaymentKey(tossPaymentKey)) {
            log.info("[시드 결제] 실제 토스 API 호출 없이 취소 처리 - cancelReason: {}, amount: {}", cancelReason, amount);
            return ResponseEntity.ok(new TossConfirmResponse("카드", amount));
        }

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


        // [로그 정리] tossPaymentKey(결제 조회·취소에 쓰는 키)가 로그에 남아서 주석 처리하고, 아래에서는 키를 뺀 정보만 남김
        // log.info("토스 결제 취소 요청 성공 tossPaymentKey: %s | cancelReason: %s | amount: %s".formatted(tossPaymentKey, cancelReason, amount));
        log.info("토스 결제 취소 요청 성공 - cancelReason: {}, amount: {}", cancelReason, amount);

        return response;
    }
}
