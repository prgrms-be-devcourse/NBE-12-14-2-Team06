package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.application.entity.Application;

import java.math.BigDecimal;
import java.time.Instant;

public record ApplicationLocationResponse(

        BigDecimal lat,
        BigDecimal lng,
        BigDecimal accuracy,
        Instant updatedAt
){
    public ApplicationLocationResponse(Application application) {
        this(
                application.getLat(),
                application.getLng(),
                application.getAccuracy(),
                application.getLocationUpdatedAt()
        );
    }
}
