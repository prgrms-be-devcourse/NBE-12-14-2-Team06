package com.back.nbe12142team06.domain.education.controller;

import com.back.nbe12142team06.domain.education.dto.EducationVideoResponse;
import com.back.nbe12142team06.domain.education.dto.WatchProgressLogCreateRequest;
import com.back.nbe12142team06.domain.education.dto.WatchProgressLogCreateResponse;
import com.back.nbe12142team06.domain.education.entity.EducationProgress;
import com.back.nbe12142team06.domain.education.service.EducationService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EducationController {

    private final EducationService educationService;

    // 하트비트 (프론트에서 10초에 한번씩 요청 + 영상 끝나면 요청)
    @PostMapping("/education-videos/{videoId}/watchlogs")
    public RsData<WatchProgressLogCreateResponse> createLog(
            @PathVariable Long videoId,
            @RequestBody @Valid WatchProgressLogCreateRequest request,
            @AuthenticationPrincipal SecurityUser me
    ){

        EducationProgress progress = this.educationService.recordProgress(me.getId(), videoId, request.positionSec());

        return new RsData<>(
                "200-1",
                "시청 기록이 저장되었습니다.",
                new WatchProgressLogCreateResponse(progress)
        );
    }

    // 영상 목록 조회
    @GetMapping("/education-videos")
    public RsData<List<EducationVideoResponse>> getVideos(
            @AuthenticationPrincipal SecurityUser me
    ) {
        List<EducationVideoResponse> responses = this.educationService.getProgresses(me.getId()).stream()
                .map(EducationVideoResponse::new)
                .toList();

        return new RsData<>(
                "200-1",
                "교육 영상 목록을 조회했습니다.",
                responses
        );
    }

    // 영상 단건 조회
    @GetMapping("/education-videos/{videoId}")
    public RsData<EducationVideoResponse> getVideo(
            @PathVariable Long videoId,
            @AuthenticationPrincipal SecurityUser me
    ) {
        EducationProgress progress = this.educationService.getProgress(me.getId(), videoId);

        return new RsData<>(
                "200-1",
                "교육 영상을 조회했습니다.",
                new EducationVideoResponse(progress)
        );
    }
}
