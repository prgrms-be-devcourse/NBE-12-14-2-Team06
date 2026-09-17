package com.back.nbe12142team06.domain.report.controller;

import com.back.nbe12142team06.domain.report.dto.ReportDto;
import com.back.nbe12142team06.domain.report.dto.ReportWriteRequest;
import com.back.nbe12142team06.domain.report.dto.ReportWriteResponse;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.report.service.ReportService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ReportController {

    private final ReportService reportService;

    // 진료 보고서 작성
    @PostMapping("/{applicationId}/report")
    public RsData<ReportWriteResponse> write(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor,
            @RequestBody @Valid ReportWriteRequest request) {

        Report report = reportService.write(applicationId, actor.getId(), request);

        return new RsData<>(
                "201-1",
                "%d번 동행 건의 보고서가 등록되었습니다.".formatted(applicationId),
                new ReportWriteResponse(report)
        );
    }

    // 진료 보고서 조회
    @GetMapping("/{applicationId}/report")
    public RsData<ReportDto> detail(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor) {

        ReportDto reportDto = new ReportDto(
                reportService.findByApplicationId(applicationId, actor.getId())
        );

        return new RsData<>("200-1", "보고서 조회 성공", reportDto);
    }
}