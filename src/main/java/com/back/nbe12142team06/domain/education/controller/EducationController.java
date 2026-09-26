package com.back.nbe12142team06.domain.education.controller;

import com.back.nbe12142team06.domain.education.dto.EducationVideoResponse;
import com.back.nbe12142team06.domain.education.dto.WatchProgressLogCreateRequest;
import com.back.nbe12142team06.domain.education.dto.WatchProgressLogCreateResponse;
import com.back.nbe12142team06.domain.education.entity.EducationProgress;
import com.back.nbe12142team06.domain.education.service.EducationService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "교육", description = "동행 매니저 교육 영상 시청 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/education-videos")
public class EducationController {

    private final EducationService educationService;

    // 하트비트 (프론트에서 5초에 한번씩 요청 + 영상 끝나면 요청)
    @Operation(summary = "시청 기록", description = "현재 재생 위치를 기록합니다. 재생 시작 시, 재생 중 5초마다, 영상 종료 시 호출합니다. 건너뛰기·배속 시청은 인정되지 않으며, 필수 영상을 모두 완료하면 교육 이수 처리됩니다.")
    @PostMapping("/{videoId}/watchlogs")
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
    @Operation(summary = "교육 영상 목록 조회", description = "교육 영상 목록과 로그인한 동행 매니저의 영상별 시청 진행 상황을 조회합니다.")
    @GetMapping
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
    @Operation(summary = "교육 영상 단건 조회", description = "특정 교육 영상과 로그인한 동행 매니저의 시청 진행 상황을 조회합니다.")
    @GetMapping("/{videoId}")
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
