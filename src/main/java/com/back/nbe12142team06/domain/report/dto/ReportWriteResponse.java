package com.back.nbe12142team06.domain.report.dto;

import com.back.nbe12142team06.domain.report.entity.Report;

import java.time.LocalDateTime;

public record ReportWriteResponse(
        Long id,
        Long applicationId,
        String title,
        String originContent,
        String aiSummary,
        LocalDateTime summarizedAt,
        LocalDateTime createdAt
) {
    public ReportWriteResponse(Report report) {
        this(
                report.getId(),
                report.getApplication().getId(),
                report.getTitle(),
                report.getOriginContent(),
                report.getAiSummary(),      // AI 요약 전이므로 현재는 null
                report.getSummarizedAt(),   // 동일
                report.getCreatedAt()
        );
    }
}