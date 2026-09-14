package com.back.nbe12142team06.domain.payment.controller;

import com.back.nbe12142team06.domain.payment.dto.*;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.service.PaymentService;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.response.RsData;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{paymentId}/confirm")
    public RsData<PaymentConfirmResponse> requestConfirm(@RequestParam Long userId,
                                                         @RequestBody PaymentConfirmRequest request,
                                                         @PathVariable Long paymentId) {
        // 현재는 쿼리로 받도록 설정 -> 나중에 AccessToken 도입 후 리팩터링

        // 결제 승인 요청 로직, 실패 시 예외(400-11) 발생
        try {
            paymentService.confirm(request, paymentId, 1L);
        } catch (InvalidException e) {
            throw e;
        } catch (Exception e) {
            // 기타 DB 저장 하다 예외 발생하는 경우 -> 결제 취소
            cancelPayment(userId, paymentId, new PaymentCancelRequest("서버 에러 발생"));
            throw new InternalServerErrorException(10, "결제 승인 도중 서버 에러가 발생했습니다.");
        }

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

        if (amount == null || !amount.equals(request.amount())) {
            throw new InvalidException(10, "결제 금액 정보가 유효하지 않습니다.");
        }

        return new RsData<>("200-n", "결제 정보가 유효합니다.");
    }

    @GetMapping
    public RsData<?> paymentList(@RequestParam Long userId) {
        // 현재는 쿼리로 받도록 설정 -> 나중에 AccessToken 도입 후 리팩터링

        List<Payment> payments = paymentService.findAll(userId);

        return new RsData<>("200-n", "결제 정보를 불러왔습니다.",
                payments.stream().map(PaymentResponse::new));
    }

    @GetMapping("/{paymentId}")
    public RsData<?> getPayment(@RequestParam Long userId, @PathVariable Long paymentId) {
        // 현재는 쿼리로 받도록 설정 -> 나중에 AccessToken 도입 후 리팩터링

        Payment payment = paymentService.findById(userId, paymentId);

        return new RsData<>("200-n", "결제 정보를 불러왔습니다.",
                new PaymentResponse(payment));
    }

    @DeleteMapping("/{paymentId}")
    public RsData<?> cancelPayment(@RequestParam Long userId, @PathVariable Long paymentId,
                                   @RequestBody PaymentCancelRequest request) {
        // 현재는 쿼리로 받도록 설정 -> 나중에 AccessToken 도입 후 리팩터링

        paymentService.cancel(userId, paymentId, request);

        return new RsData<>("201-n", "결제 취소 성공했습니다.");
    }
}
