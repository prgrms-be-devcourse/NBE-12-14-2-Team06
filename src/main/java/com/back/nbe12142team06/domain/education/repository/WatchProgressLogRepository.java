package com.back.nbe12142team06.domain.education.repository;

import com.back.nbe12142team06.domain.education.entity.EducationProgress;
import com.back.nbe12142team06.domain.education.entity.WatchProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchProgressLogRepository extends JpaRepository<WatchProgressLog, Long> {
    void deleteAllByEducationProgressIn(List<EducationProgress> progresses);
}
