package com.back.nbe12142team06.domain.user.dto.profile;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;

public record EscortSignUpResponse(
    Long id,
    String bankName,
    String accountHolder,
    String accountNumber
) {
    public EscortSignUpResponse(EscortProfile escortProfile){
        this(
                escortProfile.getUserId(),
                escortProfile.getBankName(),
                escortProfile.getAccountHolder(),
                escortProfile.getAccountNumber()
        );
    }
}
