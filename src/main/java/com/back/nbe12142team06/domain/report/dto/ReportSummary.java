package com.back.nbe12142team06.domain.report.dto;

// LLM 요약 결과 (JSON 으로 직렬화되어 ai_summary 컬럼에 저장)
// 원문에 해당 내용이 없으면 각 필드는 null
public record ReportSummary(
        String visitPurpose,     // 방문 목적
        String diagnosis,        // 의사 소견
        String prescription,     // 처방 내역
        String nextVisit,        // 다음 방문 일정
        String caregiverNote     // 보호자가 챙겨야 할 사항
) {
}