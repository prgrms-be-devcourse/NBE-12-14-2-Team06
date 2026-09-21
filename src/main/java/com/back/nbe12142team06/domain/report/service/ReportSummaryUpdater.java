package com.back.nbe12142team06.domain.report.service;

import com.back.nbe12142team06.domain.report.dto.ReportSummary;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.report.repository.ReportRepository;
import com.back.nbe12142team06.global.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 트랜잭션 경계를 분리하기 위한 클래스.
 * 같은 서비스 안에서 메서드를 호출하면 프록시를 거치지 않아 @Transactional 이 적용되지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportSummaryUpdater {

    private final ReportRepository reportRepository;
    private final ReportMasker reportMasker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public String loadMaskedContent(Long reportId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(3, "존재하지 않는 보고서입니다."));

        return reportMasker.mask(
                report.getOriginContent(),
                report.getApplication().getPost().getClient(),
                report.getApplication().getEscort()
        );
    }

    @Transactional
    public void saveSummary(Long reportId, ReportSummary summary) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(3, "존재하지 않는 보고서입니다."));

        try {
            report.applySummary(objectMapper.writeValueAsString(summary));
        } catch (JsonProcessingException e) {
            log.error("요약 결과 직렬화 실패 - reportId: {}", reportId, e);
        }
    }
}