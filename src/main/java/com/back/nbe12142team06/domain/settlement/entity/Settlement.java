package com.back.nbe12142team06.domain.settlement.entity;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.global.entity.BaseSoftDeleteTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 정산 엔티티
 * 플랫폼 -> 동행 매니저 정산
 */
@Entity
@Builder
@Getter
@Table(
        uniqueConstraints = {@UniqueConstraint(
                name = "UK_payment_application",
                columnNames = {
                        "payment_id",
                        "application_id"
                }
        )}
)
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
    @Builder.Default
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    // 정산 일자
    private LocalDate settledDate;

    // 결제, Settlement 생성은 지원 승인이 되면 생성 -> 지원이 취소되면 정산도 삭제
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    // 의뢰 지원
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    // 동행 매니저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escort_id")
    private User escort;

    public void settlementDone() {
        this.settlementStatus = SettlementStatus.COMPLETED;
    }

    public void settlementDateUpdate(LocalDate updateDate) {
        this.settledDate = updateDate;
    }
}
