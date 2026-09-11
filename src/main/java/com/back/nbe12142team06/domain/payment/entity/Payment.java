package com.back.nbe12142team06.domain.payment.entity;

import com.back.nbe12142team06.global.entity.BaseSoftDeleteTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 엔티티
 * 의뢰인 -> 플랫폼 결제
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseSoftDeleteTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제 금액, 최소 0원 보다 커야함
    @Column(
            nullable = false,
            check = @CheckConstraint(name = "chk_amount", constraint = "amount > 0")
    )
    private int amount;

    // 시간 당 금액
    @Column(nullable = false)
    private int hourlyPaySnapshot;

    // 동행 시간
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "24.0")
    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal hours;

    // 토스 주문 번호
    @Column(nullable = false, unique = true)
    private String orderId;

    // 토스 결제 키
    @Column(nullable = false, unique = true)
    private String paymentKey;

    // 결제 수단, 현재 서비스는 카드 또는 계좌이체
    private String method;

    // 결제 처리 상태, 기본값 준비 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.READY;

    // 승인 날짜
    private LocalDateTime approvedAt;

    // 취소 날짜
    private LocalDateTime canceledAt;

    // 취소 사유
    private String cancelReason;

    // 결제 취소 할 수 있는 금액, 최소 0원 이상이어야 함
    @Column(
            nullable = false,
            check = @CheckConstraint(name = "chk_balance_amount", constraint = "balance_amount >= 0")
    )
    private int balanceAmount = 0;

    // 결제를 취소/재결제 할 수 있기 때문에 N:1
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id")
//    private Post post;
}
