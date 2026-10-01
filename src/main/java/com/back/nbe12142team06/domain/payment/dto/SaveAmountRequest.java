package com.back.nbe12142team06.domain.payment.dto;

public record SaveAmountRequest(Long paymentId, String orderId, String amount) {
}
