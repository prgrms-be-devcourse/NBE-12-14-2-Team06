package com.back.nbe12142team06.domain.education.dto;

import com.back.nbe12142team06.domain.education.entity.EducationProgress;

public record EducationVideoResponse(
        Long videoId,
        String title,
        String url,
        int durationSec,
        boolean required,
        // 서버가 인정한 재생 위치 (이어보기 위치)
        double maxWatchedSec,
        // 해당 영상 시청 완료 여부
        boolean completed
) {
    public EducationVideoResponse(EducationProgress progress) {
        this(
                progress.getEducationVideo().getId(),
                progress.getEducationVideo().getTitle(),
                progress.getEducationVideo().getUrl(),
                progress.getEducationVideo().getDurationSec(),
                progress.getEducationVideo().isRequired(),
                progress.getMaxWatchedSec(),
                progress.isCompleted()
        );
    }
}