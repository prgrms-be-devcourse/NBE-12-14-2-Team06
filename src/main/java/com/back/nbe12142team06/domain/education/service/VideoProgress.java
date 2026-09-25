package com.back.nbe12142team06.domain.education.service;

import com.back.nbe12142team06.domain.education.entity.EducationVideo;

public record VideoProgress(
        EducationVideo video,
        double maxWatchedSec,
        boolean completed
) {
}