package com.back.nbe12142team06.domain.application.dto;

public record ApplicationEscortProfileResponse(
        Long escortId,
        String name,
        Boolean verified,
        String intro,
        Integer completedCount,
        Double rating,
        Integer ratingCount,
        Integer noShowCount
){

}
