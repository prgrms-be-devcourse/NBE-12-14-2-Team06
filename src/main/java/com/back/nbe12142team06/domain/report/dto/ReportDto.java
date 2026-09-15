package com.back.nbe12142team06.domain.report.dto;

import com.back.nbe12142team06.domain.report.entity.Report;

import java.time.LocalDateTime;

// 진료 보고서 조회용
public record ReportDto(
        Long id,
        Long applicationId,
        String title,
        String originContent,
        String aiSummary,
        LocalDateTime summarizedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public ReportDto(Report report) {
        this(
                report.getId(),
                report.getApplication().getId(),
                report.getTitle(),
                report.getOriginContent(),
                report.getAiSummary(),
                report.getSummarizedAt(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}