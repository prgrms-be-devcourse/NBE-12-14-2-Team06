package com.back.nbe12142team06.domain.report.controller;

import com.back.nbe12142team06.domain.report.dto.ReportDto;
import com.back.nbe12142team06.domain.report.dto.ReportWriteRequest;
import com.back.nbe12142team06.domain.report.dto.ReportWriteResponse;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.report.service.ReportService;
import com.back.nbe12142team06.domain.report.service.ReportSummaryService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "보고서", description = "동행 진료 보고서 작성 및 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ReportController {

    private final ReportService reportService;
    private final ReportSummaryService reportSummaryService;

    @Operation(
            summary = "진료 보고서 작성",
            description = "특정 동행 건의 진료 보고서를 작성하고 저장된 내용을 기반으로 AI 요약을 생성합니다."
    )
    @PostMapping("/{applicationId}/report")
    public RsData<ReportWriteResponse> write(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor,
            @RequestBody @Valid ReportWriteRequest request) {

        // 원문 저장 (트랜잭션 종료)
        Report report = reportService.write(applicationId, actor.getId(), request);

        // AI 요약 (트랜잭션 밖에서 외부 API 호출)
        // 실패하더라도 원문은 이미 저장되어 있으므로 예외를 전파하지 않는다
        reportSummaryService.summarize(report.getId());

        return new RsData<>(
                "201-1",
                "%d번 동행 건의 보고서가 등록되었습니다.".formatted(applicationId),
                new ReportWriteResponse(reportService.findByApplicationId(applicationId, actor.getId()))
        );
    }

    @Operation(
            summary = "진료 보고서 조회",
            description = "특정 동행 건의 진료 보고서를 조회합니다."
    )
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