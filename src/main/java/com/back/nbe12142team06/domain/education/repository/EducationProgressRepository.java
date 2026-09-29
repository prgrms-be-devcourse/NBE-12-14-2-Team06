package com.back.nbe12142team06.domain.education.repository;

import com.back.nbe12142team06.domain.education.entity.EducationProgress;
import com.back.nbe12142team06.domain.education.entity.EducationVideo;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationProgressRepository extends JpaRepository<EducationProgress, Long> {
    List<EducationProgress> findAllByEscortProfileUserId(Long userId);

    long countByEscortProfileAndCompletedTrueAndEducationVideoRequiredTrue(EscortProfile escortProfile);

    Optional<EducationProgress> findByEscortProfileAndEducationVideo(EscortProfile escortProfile, EducationVideo educationVideo);

    @EntityGraph(attributePaths = "educationVideo")
    List<EducationProgress> findAllByEscortProfileUserIdOrderByEducationVideoIdAsc(Long userId);

    @EntityGraph(attributePaths = "educationVideo")
    Optional<EducationProgress> findByEscortProfileUserIdAndEducationVideoId(Long userId, Long videoId);
}
