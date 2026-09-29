package com.back.nbe12142team06.domain.payment.controller;

import com.back.nbe12142team06.domain.payment.dto.*;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.service.PaymentService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "결제", description = "결제 승인, 검증, 조회 및 취소 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "결제 승인",
            description = "결제 요청 정보를 확인하고 결제를 승인합니다."
    )
    @PostMapping("/{paymentId}/confirm")
    public RsData<PaymentConfirmResponse> requestConfirm(@AuthenticationPrincipal SecurityUser actor,
                                                         @RequestBody PaymentConfirmRequest request,
                                                         @PathVariable Long paymentId,
                                                         HttpSession session) {
        Long userId = actor.getId();
        String amount = (String) session.getAttribute("amount");

        paymentService.confirm(request, paymentId, userId, amount);

        return new RsData<>("200-10", "결제 승인에 성공했습니다.",
                new PaymentConfirmResponse(request));
    }

    @Operation(
            summary = "결제 정보 임시 저장",
            description = "결제 진행에 필요한 주문 ID와 결제 금액을 세션에 임시 저장합니다."
    )
    @PostMapping("/save-amount")
    public RsData<?> tempSave(HttpSession session, @RequestBody SaveAmountRequest request) {
        session.setAttribute("orderId", request.orderId());
        session.setAttribute("amount", request.amount());

        log.info("세션 저장 완료 - orderId: %s, amount: %s".formatted(request.orderId(), request.amount()));

        return new RsData<>("201-n", "결제 정보 임시 저장에 성공했습니다.");
    }

    @Operation(
            summary = "결제 금액 검증",
            description = "세션에 저장된 결제 금액과 요청 정보를 비교하여 결제 정보를 검증합니다."
    )
    @PostMapping("/verify-amount")
    public RsData<?> verifyAmount(HttpSession session, @RequestBody SaveAmountRequest request) {
        String orderId = (String) session.getAttribute("orderId");
        String amount = (String) session.getAttribute("amount");

        paymentService.verifyAmount(amount, request);

        return new RsData<>("200-n", "결제 정보가 유효합니다.");
    }

    @Operation(
            summary = "결제 목록 조회",
            description = "현재 로그인한 사용자의 결제 내역을 조회합니다."
    )
    @GetMapping
    public RsData<?> paymentList(@AuthenticationPrincipal SecurityUser actor) {
        Long userId = actor.getId();

        List<Payment> payments = paymentService.findAll(userId);

        return new RsData<>("200-n", "결제 정보를 불러왔습니다.",
                payments.stream().map(PaymentResponse::new));
    }

    @Operation(
            summary = "결제 상세 조회",
            description = "현재 로그인한 사용자의 특정 결제 정보를 조회합니다."
    )
    @GetMapping("/{paymentId}")
    public RsData<?> getPayment(@AuthenticationPrincipal SecurityUser actor,
                                @PathVariable Long paymentId) {
        Long userId = actor.getId();

        Payment payment = paymentService.findById(userId, paymentId);

        return new RsData<>("200-n", "결제 정보를 불러왔습니다.",
                new PaymentResponse(payment));
    }

    @Operation(
            summary = "결제 취소",
            description = "현재 로그인한 사용자의 특정 결제를 취소합니다."
    )
    @DeleteMapping("/{paymentId}")
    public RsData<?> cancelPayment(@AuthenticationPrincipal SecurityUser actor,
                                   @PathVariable Long paymentId,
                                   @RequestBody PaymentCancelRequest request) {
        Long userId = actor.getId();

        paymentService.cancel(userId, paymentId, request);

        return new RsData<>("201-n", "결제 취소 성공했습니다.");
    }

    @Operation(
            summary = "공고별 결제 조회",
            description = "현재 로그인한 사용자의 공고별 결제 정보를 조회합니다."
    )
    @GetMapping("/posts/{postId}")
    public RsData<?> getPaymentByPost(@AuthenticationPrincipal SecurityUser actor,
                                      @PathVariable Long postId) {
        Long userId = actor.getId();

        Payment payment = paymentService.findByPostIdAndReady(postId, userId);

        return new RsData<>("200-n", "결제 정보를 불러왔습니다.",
                payment == null ? null : new PaymentResponse(payment));
    }
}
