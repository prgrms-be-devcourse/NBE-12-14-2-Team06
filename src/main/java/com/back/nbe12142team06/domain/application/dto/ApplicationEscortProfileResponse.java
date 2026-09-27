package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.user.enums.EscortGrade;

public record ApplicationEscortProfileResponse(
        Long escortId,
        String name,
        Boolean verified,
        String intro,
        Integer completedCount,
        Double rating,
        Integer ratingCount,
        Integer noShowCount,
        EscortGrade grade
){

}
