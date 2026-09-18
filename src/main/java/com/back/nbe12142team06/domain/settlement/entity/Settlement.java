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

    // 의뢰 지원
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", unique = true)
    private Application application;

    // 동행 매니저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escort_id")
    private User escort;

    private void updateStatus(SettlementStatus status) {
        this.settlementStatus = status;
    }

    public void settlementDone() {
        updateStatus(SettlementStatus.COMPLETED);
    }

    public void settlementFailed() {
        updateStatus(SettlementStatus.FAILED);
    }

    public void settlementDateUpdate(LocalDate updateDate) {
        this.settledDate = updateDate;
    }
}
