package com.back.nbe12142team06.domain.payment.controller;

import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.dto.SaveAmountRequest;
import com.back.nbe12142team06.domain.payment.service.PaymentService;
import com.back.nbe12142team06.global.response.RsData;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public RsData<?> requestConfirm(@RequestBody PaymentConfirmRequest request) {
        RsData<?> response = paymentService.confirm(request);

        return response;
    }

    @PostMapping("/saveAmount")
    public RsData<?> tempSave(HttpSession session, @RequestBody SaveAmountRequest request) {
        session.setAttribute("orderId", request.orderId());
        session.setAttribute("amount", request.amount());

        return new RsData<>("200-n", "결제 정보 임시 저장에 성공했습니다.");
    }
}
