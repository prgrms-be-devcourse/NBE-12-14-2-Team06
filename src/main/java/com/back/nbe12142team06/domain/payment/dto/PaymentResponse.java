package com.back.nbe12142team06.domain.payment.dto;

import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        int amount,
        int hourlyPaySnapshot,
        BigDecimal hours,
        String orderId,
        PaymentStatus paymentStatus,
        LocalDateTime approvedAt,
        LocalDateTime canceledAt,
        String cancelReason
        ) {
    public PaymentResponse(Payment payment) {
        this(
                payment.getId(),
                payment.getAmount(),
                payment.getHourlyPaySnapshot(),
                payment.getHours(),
                payment.getOrderId(),
                payment.getPaymentStatus(),
                payment.getApprovedAt(),
                payment.getCanceledAt(),
                payment.getCancelReason()
                );
    }
}
