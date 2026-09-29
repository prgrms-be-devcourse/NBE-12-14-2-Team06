package com.back.nbe12142team06.domain.user.dto.profile;

import com.back.nbe12142team06.domain.user.entity.ClientProfile;

public record ClientProfileModifyResponse(
        Long userId,
        String emergencyContactName,
        String emergencyContactPhone,
        String careNote
) {
    public ClientProfileModifyResponse(ClientProfile profile) {
        this(
                profile.getUserId(),
                profile.getEmergencyContactName(),
                profile.getEmergencyContactPhone(),
                profile.getCareNote()
        );
    }
}

