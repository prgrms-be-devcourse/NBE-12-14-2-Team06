package com.back.nbe12142team06.domain.application.service;

import com.back.nbe12142team06.domain.application.dto.ApplicationAcceptResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationApplyResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationListResponse;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.entity.EscortProgressLog;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.repository.EscortProgressLogRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EscortRepository escortRepository;
    private final EscortProgressLogRepository escortProgressLogRepository;

    @Transactional
    public ApplicationApplyResponse apply(Long postId, Long userId) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException("공고를 찾을 수 없습니다."));

        // 모집 중인 공고만 지원 가능
        if (post.getPostStatus() != PostStatus.OPEN) {
            throw new InvalidException("모집 중인 공고에만 지원할 수 있습니다.");
        }

        User escort = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        // 동행인만 지원 가능
        if (escort.getRole() != Role.ESCORT) {
            throw new InvalidException("동행인만 공고에 지원할 수 있습니다.");
        }

        // 동일 공고 중복 지원 방지
        if (applicationRepository.existsByPostAndEscort(post, escort)) {
            throw new DuplicatedException("이미 지원한 공고입니다.");
        }

        Application application = Application.builder()
                .post(post)
                .escort(escort)
                .build();

        Application savedApplication = applicationRepository.save(application);
        return new ApplicationApplyResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationListResponse> list(Long postId, Long userId, Pageable pageable) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException("공고를 찾을 수 없습니다."));

        if (!post.getClient().getId().equals(userId)) {
            throw new ForbiddenException("본인 공고의 지원 목록만 조회할 수 있습니다.");
        }

        return applicationRepository
                .findAllByPostIdWithEscort(postId, pageable)
                .map(ApplicationListResponse::new);
    }

    @Transactional
    public ApplicationAcceptResponse accept(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId).orElseThrow(
                () -> new NotFoundException("지원을 찾을 수 없습니다."));

        Post post = application.getPost();
        User escort = application.getEscort();

        // 본인 공고에 들어온 지원만 승인 가능
        if (!post.getClient().getId().equals(userId)) {
            throw new ForbiddenException("본인 공고의 지원만 승인할 수 있습니다.");
        }

        // 대기 중인 지원만 승인 가능
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidException("대기 중인 지원만 승인할 수 있습니다.");
        }

        // 모집 중인 공고만 매칭 가능
        if (post.getPostStatus() != PostStatus.OPEN) {
            throw new InvalidException("모집 중인 공고만 매칭할 수 있습니다.");
        }

        // 이미 ACCEPTED된 다른 공고와 동행 시간이 겹치는지 확인
        List<Application> acceptedApplications =
                applicationRepository.findAllByEscortAndStatus(
                        escort,
                        ApplicationStatus.ACCEPTED
                );

        boolean hasTimeConflict = acceptedApplications.stream()
                .anyMatch(acceptedApplication ->
                        isTimeOverlapping(post, acceptedApplication.getPost())
                );

        if (hasTimeConflict) {
            throw new InvalidException("이미 매칭된 다른 공고와 동행 시간이 겹칩니다.");
        }

        // 현재 공고의 다른 PENDING 지원자 조회
        List<Application> samePostPendingApplications =
                applicationRepository.findAllByPostAndStatus(
                        post,
                        ApplicationStatus.PENDING
                );

        // 같은 동행인이 지원한 다른 PENDING 공고 조회
        List<Application> escortPendingApplications =
                applicationRepository.findAllByEscortAndStatus(
                        escort,
                        ApplicationStatus.PENDING
                );

        // 선택된 지원 승인 + 공고 매칭
        application.accept();
        post.match();

        // 같은 공고의 나머지 지원자 자동 거절
        samePostPendingApplications.stream()
                .filter(otherApplication ->
                        !otherApplication.getId().equals(applicationId))
                .forEach(Application::reject);

        // 같은 동행인의 다른 지원 중 시간이 겹치는 지원 자동 거절
        escortPendingApplications.stream()
                .filter(otherApplication ->
                        !otherApplication.getId().equals(applicationId))
                .filter(otherApplication ->
                        isTimeOverlapping(post, otherApplication.getPost()))
                .forEach(Application::reject);

        return new ApplicationAcceptResponse(application);
    }

    @Transactional
    public void reject(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원을 찾을 수 없습니다."));

        Post post = application.getPost();

        // 본인 공고에 들어온 지원만 거절 가능
        if (!post.getClient().getId().equals(userId)) {
            throw new ForbiddenException("본인 공고의 지원만 거절할 수 있습니다.");
        }

        // 대기 중인 지원만 거절 가능
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidException("대기 중인 지원만 거절할 수 있습니다.");
        }

        application.reject();
    }

    private boolean isTimeOverlapping(Post firstPost, Post secondPost) {
        return firstPost.getEscortStartAt().isBefore(secondPost.getEscortEndAt())
                && firstPost.getEscortEndAt().isAfter(secondPost.getEscortStartAt());
    }

    @Transactional
    public void cancel(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원을 찾을 수 없습니다."));

        // 본인이 지원한 내역만 취소 가능
        if (!application.getEscort().getId().equals(userId)) {
            throw new ForbiddenException("본인이 지원한 내역만 취소할 수 있습니다.");
        }

        // 대기 상태에서 지원 취소
        if (application.getStatus() == ApplicationStatus.PENDING) {
            application.cancel();
            return;
        }

        // 승인 후 동행인 취소
        if (application.getStatus() == ApplicationStatus.ACCEPTED) {

            Post post = application.getPost();

            EscortProfile escortProfile = escortRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("동행인 프로필을 찾을 수 없습니다."));

            application.noShow();
            escortProfile.increaseNoShowCount();

            // 모집 마감 전이면 다시 동행인을 모집
            if (LocalDateTime.now().isBefore(post.getRecruitEndAt())) {
                post.reopen();
            } else {
                // 모집 마감 후라면 공고 취소
                post.matchedCancel();
            }
            return;
        }

        throw new InvalidException("취소할 수 없는 지원 상태입니다.");
    }

    @Transactional
    public void updateProgress(Long applicationId, Long userId, EscortProgress progress) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원을 찾을 수 없습니다."));

        // 본인의 동행 진행 상태만 변경 가능
        if (!application.getEscort().getId().equals(userId)) {
            throw new ForbiddenException("본인의 동행 진행 상태만 변경할 수 있습니다.");
        }

        // 승인된 지원만 동행 진행 상태 변경 가능
        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new InvalidException("승인된 지원만 동행 진행 상태를 변경할 수 있습니다.");
        }

        EscortProgress currentProgress = escortProgressLogRepository
                .findTopByApplicationOrderByOccurredAtDescIdDesc(application)
                .map(log -> log.getProgress())
                .orElse(EscortProgress.NOT_STARTED);

        EscortProgress nextProgress = switch (currentProgress) {
            case NOT_STARTED -> EscortProgress.DEPARTED;
            case DEPARTED -> EscortProgress.TO_HOSPITAL;
            case TO_HOSPITAL -> EscortProgress.AT_HOSPITAL;
            case AT_HOSPITAL -> EscortProgress.TO_HOME;
            case TO_HOME -> EscortProgress.ARRIVED_HOME;
            case ARRIVED_HOME -> null;
        };

        if (nextProgress == null) {
            throw new InvalidException("이미 동행이 완료되었습니다.");
        }

        if (progress != nextProgress) {
            throw new InvalidException("동행 진행 상태를 순서대로 변경해야 합니다.");
        }

        LocalDateTime occurredAt = LocalDateTime.now();

        EscortProgressLog progressLog = EscortProgressLog.builder()
                .application(application)
                .progress(progress)
                .occurredAt(occurredAt)
                .build();

        escortProgressLogRepository.save(progressLog);

        if (progress == EscortProgress.DEPARTED) {
            application.getPost().startProgress(occurredAt);
        }
    }
}
