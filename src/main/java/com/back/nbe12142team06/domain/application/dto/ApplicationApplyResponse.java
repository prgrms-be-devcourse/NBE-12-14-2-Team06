package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;

public record ApplicationApplyResponse(
        Long id,
        Long postId,
        ApplicationStatus status
){
    public ApplicationApplyResponse(Application application) {
        this(
                application.getId(),
                application.getPost().getId(),
                application.getStatus()
        );
    }
}
