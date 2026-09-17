package com.back.nbe12142team06.domain.application.controller;

import com.back.nbe12142team06.domain.application.dto.ApplicationAcceptResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationApplyResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationListResponse;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

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

    @GetMapping("/posts/{postId}")
    public RsData<List<ApplicationListResponse>> list(
            @PathVariable Long postId,
            @AuthenticationPrincipal SecurityUser actor) {

        List<ApplicationListResponse> responses = applicationService.list(postId, actor.getId());

        return new RsData<>(
            "200-1",
            "지원 목록 조회가 완료되었습니다.",
                responses
        );
    }

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

}
