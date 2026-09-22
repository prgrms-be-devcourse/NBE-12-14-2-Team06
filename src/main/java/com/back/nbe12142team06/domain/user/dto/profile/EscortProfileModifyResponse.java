package com.back.nbe12142team06.domain.user.dto.profile;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;

public record EscortProfileModifyResponse(
        Long userId,
        String name,
        String region,
        String intro,
        Double averageRating,
        Integer completedCount,
        Boolean verified,
        String bankName,
        String accountHolder,
        String accountNumber
) {
    public EscortProfileModifyResponse(EscortProfile escortProfile){
        this(
                escortProfile.getUserId(),
                escortProfile.getUser().getName(),
                escortProfile.getUser().getRegion(),
                escortProfile.getIntro(),
                escortProfile.getAverageRating(),
                escortProfile.getCompletedCount(),
                escortProfile.getVerified(),
                escortProfile.getBankName(),
                escortProfile.getAccountHolder(),
                escortProfile.getAccountNumber()
        );
    }
}