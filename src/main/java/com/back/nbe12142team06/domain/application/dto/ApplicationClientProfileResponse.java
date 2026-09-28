package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.user.enums.EscortGrade;

public record ApplicationClientProfileResponse(
        Long clientId,
        String clientName,
        String clientPhone,
        String emergencyContactName,
        String emergencyContactPhone,
        String careNote
){

}
