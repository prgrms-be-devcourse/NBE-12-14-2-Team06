package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.post.entity.PostStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyApplicationResponse(
        Long applicationId,
        Long postId,
        ApplicationStatus applicationStatus,
        PostStatus postStatus,
        String title,
        String hospitalName,
        String region,
        String hospitalAddress,
        LocalDateTime escortStartAt,
        LocalDateTime escortEndAt,
        int hourlyPay,
        BigDecimal escortHours,
        String content
) {
    public MyApplicationResponse(Application application) {
        this(
                application.getId(),
                application.getPost().getId(),
                application.getStatus(),
                application.getPost().getPostStatus(),
                application.getPost().getTitle(),
                application.getPost().getHospitalName(),
                application.getPost().getRegion(),
                application.getPost().getHospitalAddress(),
                application.getPost().getEscortStartAt(),
                application.getPost().getEscortEndAt(),
                application.getPost().getHourlyPay(),
                application.getPost().getEscortHours(),
                application.getPost().getContent()
        );
    }
}
