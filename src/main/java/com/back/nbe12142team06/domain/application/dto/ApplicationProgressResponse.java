package com.back.nbe12142team06.domain.application.dto;

import com.back.nbe12142team06.domain.application.enums.EscortProgress;

/** GET /api/v1/applications/{applicationId}/progress 응답 — 이 지원의 현재 동행 진행 단계 */
public record ApplicationProgressResponse(
        Long applicationId,
        EscortProgress progress
) {
}
