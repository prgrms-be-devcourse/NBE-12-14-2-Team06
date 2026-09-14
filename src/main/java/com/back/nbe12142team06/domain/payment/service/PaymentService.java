package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.response.RsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PostService postService;
    private final ObjectMapper objectMapper;
    private final RestClient tossRestClient;

    @Transactional
    public Payment confirm(PaymentConfirmRequest request, Long postId) {

        String tossPaymentKey = request.paymentKey();
        String tossOrderId = request.orderId();
        String amount = request.amount();

        System.out.println("tossPaymentKey = " + tossPaymentKey);
        System.out.println("tossOrderId = " + tossOrderId);
        System.out.println("amount = " + amount);

        Post post = postService.findById(postId);
        // TODO: 본인 공고 글이 맞는 지 검증

        Duration diff = Duration.between(post.getEscortStartAt(), post.getEscortEndAt());

        Payment payment = Payment.builder()
                .paymentKey(tossPaymentKey)
                .orderId(tossOrderId)
                .post(post)
                .hourlyPaySnapshot(post.getHourlyPay())
                .hours(BigDecimal.valueOf(diff.toMinutes() / 60))
                .build();

        paymentRepository.save(payment);

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

        return payment;
    }
}
