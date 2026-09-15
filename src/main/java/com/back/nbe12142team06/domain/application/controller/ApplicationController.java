package com.back.nbe12142team06.domain.application.controller;

import com.back.nbe12142team06.domain.application.dto.ApplicationApplyResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationListResponse;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping("/{postId}")
    public RsData<ApplicationApplyResponse> apply(@PathVariable Long postId, @RequestParam String username) {
        // 아직 JWT 적용 전이라 username을 임시로 받음
        ApplicationApplyResponse response = applicationService.apply(postId, username);

        return new RsData<>(
                "201-1",
                "지원이 완료되었습니다.",
                response
        );
    }

    @GetMapping("/posts/{postId}")
    public RsData<List<ApplicationListResponse>> list(@PathVariable Long postId) {
        List<ApplicationListResponse> responses = applicationService.list(postId);

        return new RsData<>(
            "200-1",
            "지원 목록 조회가 완료되었습니다.",
                responses
        );
    }

}
