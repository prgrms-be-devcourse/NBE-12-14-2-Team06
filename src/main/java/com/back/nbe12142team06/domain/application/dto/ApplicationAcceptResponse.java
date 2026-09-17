package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;

public record ApplicationAcceptResponse (
        Long applicationId,
        Long postId,
        Long escortId,
        ApplicationStatus status ){
    public ApplicationAcceptResponse(Application application){
        this(
                application.getId(),
                application.getPost().getId(),
                application.getEscort().getId(),
                application.getStatus()
        );
    }
}
