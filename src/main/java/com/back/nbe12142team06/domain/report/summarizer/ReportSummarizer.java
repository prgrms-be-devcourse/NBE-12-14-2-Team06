package com.back.nbe12142team06.domain.report.summarizer;

import com.back.nbe12142team06.domain.report.dto.ReportSummary;

public interface ReportSummarizer {

    /**
     * 진료 보고서 원문을 요약한다.
     * 호출 실패 시 예외를 던지며, 원문 보존은 호출 측 책임이다.
     */
    ReportSummary summarize(String maskedContent);
}