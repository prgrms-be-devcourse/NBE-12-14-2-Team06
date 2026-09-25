package com.back.nbe12142team06.domain.education.controller;

import com.back.nbe12142team06.domain.education.dto.WatchProgressLogCreateRequest;
import com.back.nbe12142team06.domain.education.dto.WatchProgressLogCreateResponse;
import com.back.nbe12142team06.domain.education.service.EducationService;
import com.back.nbe12142team06.domain.education.service.WatchLogResult;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EducationController {
    private final EducationService educationService;


    // 프론트에서 영상 시청 시 10초마다 요청해야함 + 영상 끝나면 요청
    @PostMapping("/education-videos/{videoId}/watch-logs")
    public RsData<WatchProgressLogCreateResponse> createWatchLog(
            @PathVariable Long videoId,
            @Valid @RequestBody WatchProgressLogCreateRequest request,
            @AuthenticationPrincipal SecurityUser me
    ) {
        WatchLogResult result = this.educationService.recordWatchLog(
                me.getId(),
                videoId,
                request.positionSec()
        );

        return new RsData<>(
                "200-1",
                "시청 기록이 저장되었습니다.",
                new WatchProgressLogCreateResponse(result)
        );
    }



}
