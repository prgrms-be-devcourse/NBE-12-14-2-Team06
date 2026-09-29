package com.back.nbe12142team06.domain.report.summarizer;

import com.back.nbe12142team06.domain.report.dto.ReportSummary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// 테스트 환경에서는 실제 API 를 호출하지 않는다
@Component
@Profile("test")
public class FakeReportSummarizer implements ReportSummarizer {

    @Override
    public ReportSummary summarize(String maskedContent) {
        return new ReportSummary(
                "정기 검진",
                "특이 소견 없음",
                "처방 없음",
                "1개월 후 재방문",
                "무리한 활동 자제"
        );
    }
}