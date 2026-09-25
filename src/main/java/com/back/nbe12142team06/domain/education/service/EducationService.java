package com.back.nbe12142team06.domain.education.service;

import com.back.nbe12142team06.domain.education.entity.EducationProgress;
import com.back.nbe12142team06.domain.education.entity.EducationVideo;
import com.back.nbe12142team06.domain.education.entity.WatchProgressLog;
import com.back.nbe12142team06.domain.education.repository.EducationProgressRepository;
import com.back.nbe12142team06.domain.education.repository.EducationVideoRepository;
import com.back.nbe12142team06.domain.education.repository.WatchProgressLogRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EscortProfileRepository escortProfileRepository;
    private final EducationVideoRepository educationVideoRepository;
    private final EducationProgressRepository educationProgressRepository;
    private final WatchProgressLogRepository watchProgressLogRepository;
    private final Clock clock;

    // 시청 기록
    @Transactional
    public EducationProgress recordProgress(Long userId, Long videoId, Double positionSec) {
        EscortProfile escortProfile = this.escortProfileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("동행인 프로필이 존재하지 않습니다."));

        EducationVideo educationVideo = this.educationVideoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("존재하지않는 교육 영상입니다."));

        EducationProgress progress = this.educationProgressRepository.findByEscortProfileAndEducationVideo(escortProfile, educationVideo)
                .orElseThrow(() -> new NotFoundException("교육 진행 정보가 존재하지 않습니다."));

        // 이미 이수한 경우 더 기록하지 않음
        if (escortProfile.getVerified()) {
            return progress;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        boolean accepted = progress.record(positionSec, now);
        this.watchProgressLogRepository.save(new WatchProgressLog(progress, positionSec, now, accepted));

        // 필수 영상 전부 시청 시 동행인 프로필 신원인증 처리
        if (progress.isCompleted() && isAllRequiredCompleted(escortProfile)) {
            escortProfile.verify(now);
        }

        return progress;
    }

    // 교육 영상 목록 + 진행 상황
    @Transactional(readOnly = true)
    public List<EducationProgress> getProgresses(Long userId) {
        if (!this.escortProfileRepository.existsById(userId)) {
            throw new NotFoundException("동행인 프로필이 존재하지 않습니다.");
        }

        return this.educationProgressRepository.findAllByEscortProfileUserIdOrderByEducationVideoIdAsc(userId);
    }

    // 교육 영상 단건 + 진행 상황
    @Transactional(readOnly = true)
    public EducationProgress getProgress(Long userId, Long videoId) {
        if (!this.escortProfileRepository.existsById(userId)) {
            throw new NotFoundException("동행인 프로필이 존재하지 않습니다.");
        }

        return this.educationProgressRepository.findByEscortProfileUserIdAndEducationVideoId(userId, videoId)
                .orElseThrow(() -> new NotFoundException("존재하지않는 교육 영상입니다."));
    }


    // 동행 매니저 프로필 생성 시 모든 영상에 대한 진행 상황 생성
    @Transactional
    public void createProgresses(EscortProfile escortProfile) {
        List<EducationProgress> progresses = this.educationVideoRepository.findAll().stream()
                .map(video -> new EducationProgress(escortProfile, video))
                .toList();

        this.educationProgressRepository.saveAll(progresses);
    }

    // 동행 매니저 탈퇴 시 교육 데이터 삭제 (로그 → 진행 상황 순서)
    @Transactional
    public void deleteProgresses(Long userId) {
        List<EducationProgress> progresses = this.educationProgressRepository.findAllByEscortProfileUserId(userId);

        this.watchProgressLogRepository.deleteAllByEducationProgressIn(progresses);
        this.educationProgressRepository.deleteAll(progresses);
    }

    // 필수 영상을 모두 완료했는지
    private boolean isAllRequiredCompleted(EscortProfile escortProfile) {
        // 필수 영상 중 시청 완료한 영상 수
        long completedCount = this.educationProgressRepository.countByEscortProfileAndCompletedTrueAndEducationVideoRequiredTrue(escortProfile);
        // 필수 영상 수
        long requiredCount = this.educationVideoRepository.countByRequiredTrue();

        return completedCount == requiredCount;
    }
}