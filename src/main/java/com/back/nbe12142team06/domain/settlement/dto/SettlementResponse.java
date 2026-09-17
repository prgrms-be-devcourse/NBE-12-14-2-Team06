package com.back.nbe12142team06.domain.settlement.dto;

import com.back.nbe12142team06.domain.post.dto.PostDto;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        int payoutAmount,
        int platformFee,
        SettlementStatus status,
        LocalDate settledAt,
        PostDto post
) {
    public SettlementResponse(Settlement settlement) {
        this(
                settlement.getId(),
                settlement.getPayoutAmount(),
                settlement.getPlatformFee(),
                settlement.getSettlementStatus(),
                settlement.getSettledDate(),
                new PostDto(settlement.getApplication().getPost())
        );
    }
}
