package com.back.nbe12142team06.domain.payment.dto;

public record PaymentConfirmResponse(String orderId, String paymentKey, String amount) {
    public PaymentConfirmResponse(PaymentConfirmRequest request) {
        this(
                request.orderId(),
                request.paymentKey(),
                request.amount());
    }
}
