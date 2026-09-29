package com.back.nbe12142team06.domain.report.service;

import com.back.nbe12142team06.domain.report.dto.ReportSummary;
import com.back.nbe12142team06.domain.report.summarizer.ReportSummarizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSummaryService {

    private final ReportSummarizer reportSummarizer;
    private final ReportSummaryUpdater reportSummaryUpdater;

    /**
     * 보고서 원문을 요약해 저장한다.
     * 외부 API 호출을 트랜잭션 밖에서 수행하여 커넥션 점유를 피하고,
     * 호출이 실패해도 이미 저장된 원문은 보존한다.
     */
    public void summarize(Long reportId) {

        String maskedContent = reportSummaryUpdater.loadMaskedContent(reportId);

        long startedAt = System.currentTimeMillis();

        try {
            // 외부 API 호출 (트랜잭션 밖)
            ReportSummary summary = reportSummarizer.summarize(maskedContent);

            reportSummaryUpdater.saveSummary(reportId, summary);

            log.info("보고서 AI 요약 성공 - reportId: {}, 소요시간: {}ms",
                    reportId, System.currentTimeMillis() - startedAt);

        } catch (Exception e) {
            // 요약 실패 시에도 원문은 이미 저장되어 있으므로 사용자 입력은 유실되지 않는다.
            // summarizedAt 이 null 로 남아 "요약 대기" 상태가 된다.
            log.error("보고서 AI 요약 실패 - reportId: {}, 소요시간: {}ms",
                    reportId, System.currentTimeMillis() - startedAt, e);
        }
    }
}