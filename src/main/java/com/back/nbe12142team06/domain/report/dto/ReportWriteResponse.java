package com.back.nbe12142team06.domain.report.dto;

import com.back.nbe12142team06.domain.report.entity.Report;

import java.time.LocalDateTime;

public record ReportWriteResponse(
        Long id,
        Long applicationId,
        String title,
        String department,
        String purpose,
        String originContent,
        String notes,
        String aiSummary,
        LocalDateTime summarizedAt,
        LocalDateTime createdAt
) {
    public ReportWriteResponse(Report report) {
        this(
                report.getId(),
                report.getApplication().getId(),
                report.getTitle(),
                report.getDepartment().getDescription(),
                report.getPurpose(),
                report.getOriginContent(),
                report.getNotes(),
                report.getAiSummary(),      // 요약 실패 시 null 로 남는다
                report.getSummarizedAt(),   // 동일
                report.getCreatedAt()
        );
    }
}