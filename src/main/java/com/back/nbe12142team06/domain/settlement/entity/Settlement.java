package com.back.nbe12142team06.domain.settlement.entity;

import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.global.entity.BaseSoftDeleteTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 정산 엔티티
 * 플랫폼 -> 동행 매니저 정산
 */
@Entity
//@Table(
//        uniqueConstraints = {@UniqueConstraint(
//                name = "UK_payment_application",
//                columnNames = {
//                        "payment_id",
//                        "application_id"
//                }
//        )}
//)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseSoftDeleteTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실 지급액, 최소 0원 이상이어야 함
    @Column(
            nullable = false,
            check = @CheckConstraint(name = "chk_payout_amount", constraint = "payout_amount >= 0")
    )
    private int payoutAmount;

    // 플랫폼 수수료, 최소 0원 이상이어야 함
    @Column(
            nullable = false,
            check = @CheckConstraint(name = "chk_platform_fee", constraint = "platform_fee >= 0")
    )
    private int platformFee = 0;

    // 정산 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    // 정산 일자
    private LocalDateTime settledAt;

    // 결제, Settlement 생성은 Payment 상태가 DONE이 되면 생성
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "payment_id")
//    @Column(nullable = false)
//    private Payment payment;

    // 의뢰 지원
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "application_id")
//    @Column(nullable = false)
//    private Application application;

    // 동행 매니저
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "escort_id")
//    @Column(nullable = false)
//    private User escort;
}
