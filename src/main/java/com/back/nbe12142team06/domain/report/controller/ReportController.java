package com.back.nbe12142team06.domain.report.controller;

import com.back.nbe12142team06.domain.report.dto.ReportWriteRequest;
import com.back.nbe12142team06.domain.report.dto.ReportWriteResponse;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.report.service.ReportService;
import com.back.nbe12142team06.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ReportController {

    private final ReportService reportService;

    // 진료 보고서 작성
    @PostMapping("/{applicationId}/report")
    @Transactional
    public RsData<ReportWriteResponse> write(
            @PathVariable Long applicationId,
            @RequestBody @Valid ReportWriteRequest request) {

        Report report = reportService.write(applicationId, request);

        return new RsData<>(
                "201-1",
                "%d번 동행 건의 보고서가 등록되었습니다.".formatted(applicationId),
                new ReportWriteResponse(report)
        );
    }
}