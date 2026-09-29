package com.back.nbe12142team06.domain.user.dto.profile;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;

public record EscortProfileForClientResponse(
        Long userId,
        String name,
        String region,
        String intro,
        Double averageRating,
        Integer completedCount,
        Boolean verified
) {
    public EscortProfileForClientResponse(EscortProfile escortProfile) {
        this(
                escortProfile.getUserId(),
                escortProfile.getUser().getName(),
                escortProfile.getUser().getRegion(),
                escortProfile.getIntro(),
                escortProfile.getAverageRating(),
                escortProfile.getCompletedCount(),
                escortProfile.getVerified()
        );
    }
}
