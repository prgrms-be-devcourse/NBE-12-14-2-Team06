package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.user.enums.EscortGrade;
import com.back.nbe12142team06.domain.user.enums.Gender;

public record ApplicationEscortProfileResponse(
        Long escortId,
        String name,
        Boolean verified,
        String intro,
        Integer completedCount,
        Double rating,
        Integer ratingCount,
        Integer noShowCount,
        EscortGrade grade,
        Integer age,
        Gender gender,
        String region
){

}
