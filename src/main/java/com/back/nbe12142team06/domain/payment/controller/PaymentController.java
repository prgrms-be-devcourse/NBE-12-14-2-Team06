package com.back.nbe12142team06.domain.payment.controller;

import com.back.nbe12142team06.domain.payment.dto.*;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.service.PaymentService;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

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

    @PostMapping("/saveAmount")
    public RsData<?> tempSave(HttpSession session, @RequestBody SaveAmountRequest request) {
        session.setAttribute("orderId", request.orderId());
        session.setAttribute("amount", request.amount());

        return new RsData<>("201-n", "결제 정보 임시 저장에 성공했습니다.");
    }

    @PostMapping("/verifyAmount")
    public RsData<?> verifyAmount(HttpSession session, @RequestBody SaveAmountRequest request) {
        String orderId = (String) session.getAttribute("orderId");
        String amount = (String) session.getAttribute("amount");

        paymentService.verifyAmount(amount, request);

        return new RsData<>("200-n", "결제 정보가 유효합니다.");
    }

    @GetMapping
    public RsData<?> paymentList(@AuthenticationPrincipal SecurityUser actor) {
        Long userId = actor.getId();

        List<Payment> payments = paymentService.findAll(userId);

        return new RsData<>("200-n", "결제 정보를 불러왔습니다.",
                payments.stream().map(PaymentResponse::new));
    }

    @GetMapping("/{paymentId}")
    public RsData<?> getPayment(@AuthenticationPrincipal SecurityUser actor,
                                @PathVariable Long paymentId) {
        Long userId = actor.getId();

        Payment payment = paymentService.findById(userId, paymentId);

        return new RsData<>("200-n", "결제 정보를 불러왔습니다.",
                new PaymentResponse(payment));
    }

    @DeleteMapping("/{paymentId}")
    public RsData<?> cancelPayment(@AuthenticationPrincipal SecurityUser actor,
                                   @PathVariable Long paymentId,
                                   @RequestBody PaymentCancelRequest request) {
        Long userId = actor.getId();

        paymentService.cancel(userId, paymentId, request);

        return new RsData<>("201-n", "결제 취소 성공했습니다.");
    }
}
