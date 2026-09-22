package com.back.nbe12142team06.domain.application.controller;

import com.back.nbe12142team06.domain.application.dto.*;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지원", description = "동행 지원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    @Operation(summary = "동행 지원", description = "특정 공고에 동행 지원을 신청합니다.")
    @PostMapping("/{postId}")
    public RsData<ApplicationApplyResponse> apply(
            @PathVariable Long postId,
            @AuthenticationPrincipal SecurityUser actor) {

        ApplicationApplyResponse response = applicationService.apply(postId, actor.getId());

        return new RsData<>(
                "201-1",
                "지원이 완료되었습니다.",
                response
        );
    }

    @Operation(summary = "지원 목록 조회", description = "특정 공고에 지원한 동행자 목록을 조회합니다.")
    @GetMapping("/posts/{postId}")
    public RsData<Page<ApplicationListResponse>> list(
            @PathVariable Long postId,
            @AuthenticationPrincipal SecurityUser actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<ApplicationListResponse> responses =
                applicationService.list(postId, actor.getId(), PageRequest.of(page, size));

        return new RsData<>(
                "200-1",
                "지원 목록 조회가 완료되었습니다.",
                responses
        );
    }

    @Operation(summary = "지원 승인", description = "특정 동행자의 지원을 승인합니다.")
    @PatchMapping("/{applicationId}/accept")
    public RsData<ApplicationAcceptResponse> accept(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor) {

        ApplicationAcceptResponse response = applicationService.accept(applicationId, actor.getId());

        return new RsData<>(
            "200-1",
            "지원 승인이 완료되었습니다.",
            response
        );
    }

    @Operation(summary = "지원 거절", description = "특정 동행자의 지원을 거절합니다.")
    @PatchMapping("/{applicationId}/reject")
    public RsData<Void> reject(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor) {

        applicationService.reject(applicationId, actor.getId());

        return new RsData<>(
                "200-1",
                "지원 거절이 완료되었습니다.",
                null
        );
    }

    @Operation(summary = "지원 취소", description = "동행자 본인이 지원을 취소합니다.")
    @PatchMapping("/{applicationId}/cancel")
    public RsData<Void> cancel(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor) {

        applicationService.cancel(applicationId, actor.getId());

        return new RsData<>(
                "200-1",
                "지원 취소가 완료되었습니다.",
                null
        );
    }

    @Operation(summary = "동행 진행 상태 변경", description = "승인된 동행의 진행 상태를 변경합니다.")
    @PatchMapping("/{applicationId}/progress")
    public RsData<Void> updateProgress(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor,
            @Valid @RequestBody EscortProgressRequest request) {

        applicationService.updateProgress(applicationId, actor.getId(), request.progress());

        return new RsData<>(
                "200-1",
                "동행 진행 상태가 변경되었습니다.",
                null
        );

    }

    @Operation(summary = "동행자 프로필 조회", description = "특정 지원의 동행자 프로필 정보를 조회합니다.")
    @GetMapping("/{applicationId}/escort-profile")
    public RsData<ApplicationEscortProfileResponse> profile(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor
    ) {
        ApplicationEscortProfileResponse response =
                applicationService.getEscortProfile(applicationId, actor.getId());

        return new RsData<>(
                "200-2",
                "동행자 프로필 조회가 완료되었습니다.",
                response
        );
    }

}
