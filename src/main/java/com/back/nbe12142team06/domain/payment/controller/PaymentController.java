package com.back.nbe12142team06.domain.payment.controller;

import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmResponse;
import com.back.nbe12142team06.domain.payment.dto.SaveAmountRequest;
import com.back.nbe12142team06.domain.payment.service.PaymentService;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.response.RsData;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm/{postId}")
    public RsData<PaymentConfirmResponse> requestConfirm(@RequestBody PaymentConfirmRequest request,
                                                         @PathVariable Long postId) {

        // 결제 승인 요청 로직, 실패 시 예외(400-11) 발생
        paymentService.confirm(request, postId, 1L);

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

}
