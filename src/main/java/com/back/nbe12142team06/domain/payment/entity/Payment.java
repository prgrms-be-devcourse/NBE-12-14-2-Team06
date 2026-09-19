package com.back.nbe12142team06.domain.payment.entity;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.global.entity.BaseSoftDeleteTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 엔티티
 * 의뢰인 -> 플랫폼 결제
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
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
    @Column(unique = true)
    private String orderId;

    // 토스 결제 키
    @Column(unique = true)
    private String paymentKey;

    // 결제 수단, 현재 서비스는 카드 또는 계좌이체
    private String method;

    // 결제 처리 상태, 기본값 준비 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
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
    @Builder.Default
    private int balanceAmount = 0;

    // 결제를 취소/재결제 할 수 있기 때문에 N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // 정산은 결제, 추가 결제 등 여러 개 생길 수 있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    public void statusUpdate(PaymentStatus status) {
        this.paymentStatus = status;
    }

    // 결제 승인
    public void ApprovePayment(String orderId, String paymentKey, String method) {
        this.statusUpdate(PaymentStatus.DONE);
        this.approvedAt = LocalDateTime.now();
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.method = method;
        this.balanceAmount = this.amount;
    }

    // 결제 취소 -> 새로운 결제 데이터 반환
    public Payment cancelPayment(String cancelReason) {
        this.statusUpdate(PaymentStatus.CANCELED);
        this.balanceAmount = 0;
        this.canceledAt = LocalDateTime.now();
        this.cancelReason = cancelReason;
        return Payment.builder()
                .amount(this.amount)
                .hourlyPaySnapshot(this.hourlyPaySnapshot)
                .hours(this.hours)
                .post(this.post)
                .build();
    }

    // 결제 부분 취소
    public Payment cancelPartialPayment(String cancelReason, int cancelAmount) {
        this.cancelReason = cancelReason;
        this.amount -= cancelAmount;
        this.statusUpdate(PaymentStatus.PARTIAL_CANCELED);
        return this;
    }

    public void updateSettlement(Settlement settlement) {
        this.settlement = settlement;
    }
}
