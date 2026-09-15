package com.back.nbe12142team06.domain.payment.dto;

public record PaymentConfirmRequest(String paymentKey, String orderId, String amount) {
}
