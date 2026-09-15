package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;

public record ApplicationListResponse(
        Long applicationId,
        Long escortId,
        String escortName,
        ApplicationStatus status
){
    public ApplicationListResponse(Application application) {
        this(
                application.getId(),
                application.getEscort().getId(),
                application.getEscort().getName(),
                application.getStatus()
        );
    }
}
