package com.back.nbe12142team06.domain.application.service;

import com.back.nbe12142team06.domain.application.dto.*;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.entity.EscortProgressLog;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.repository.EscortProgressLogRepository;
import com.back.nbe12142team06.domain.penalty.service.NoShowPenaltyService;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.ClientProfile;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.ClientProfileRepository;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EscortProfileRepository escortProfileRepository;
    private final EscortProgressLogRepository escortProgressLogRepository;
    private final NoShowPenaltyService noShowPenaltyService;
    private final ClientProfileRepository clientProfileRepository;

    @Transactional
    public ApplicationApplyResponse apply(Long postId, Long userId) {


        Post post = postRepository.findByIdWithLock(postId).orElseThrow(
                () -> new NotFoundException(11, "%s번 공고가 없습니다.".formatted(postId)));

        // 모집 중인 공고만 지원 가능
        if (post.getPostStatus() != PostStatus.OPEN) {
            throw new InvalidException(21, "모집 중인 공고에만 지원할 수 있습니다.");
        }

        User escort = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(1, "회원 정보를 찾을 수 없습니다."));

        // 동행인만 지원 가능
        if (escort.getRole() != Role.ESCORT) {
            throw new InvalidException(22, "동행인만 공고에 지원할 수 있습니다.");
        }

        // 프로필이 존재하는 동행인만 지원 가능
        EscortProfile escortProfile = escortProfileRepository.findById(userId)
                .orElseThrow(() -> new InvalidException(23, "동행 매니저 프로필을 등록한 후 지원할 수 있습니다."));

        // 교육을 이수한 동행인만 지원 가능
        if (!escortProfile.getVerified()) {
            throw new ForbiddenException(21, "교육 영상 시청을 완료한 후 지원할 수 있습니다.");
        }

        // 취소되지 않은 동일 공고 지원이 있으면 중복 지원 방지
        if (applicationRepository.existsByPostAndEscortAndStatusNot(post, escort, ApplicationStatus.CANCELED)) {
            throw new DuplicatedException(21, "이미 지원한 공고입니다.");
        }

        Application application = Application.builder()
                .post(post)
                .escort(escort)
                .build();

        Application savedApplication = applicationRepository.save(application);
        log.info("[지원] applicationId={}, postId={}, escortId={}", savedApplication.getId(), postId, userId);
        return new ApplicationApplyResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationListResponse> list(Long postId, Long userId, Pageable pageable) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException(11, "%s번 공고가 없습니다.".formatted(postId)));

        if (!post.getClient().getId().equals(userId)) {
            throw new ForbiddenException(22, "본인 공고의 지원 목록만 조회할 수 있습니다.");
        }

        return applicationRepository
                .findAllByPostIdWithEscort(postId, pageable)
                .map(ApplicationListResponse::new);
    }

    @Transactional
    public ApplicationAcceptResponse accept(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId).orElseThrow(
                () -> new NotFoundException(24, "지원을 찾을 수 없습니다."));

        Long postId = application.getPost().getId();

        Post post = postRepository.findByIdWithLock(postId).orElseThrow(
                () -> new NotFoundException(11, "%s번 공고가 없습니다.".formatted(postId)));

        User escort = application.getEscort();

        // 본인 공고에 들어온 지원만 승인 가능
        if (!post.getClient().getId().equals(userId)) {
            throw new ForbiddenException(23, "본인 공고의 지원만 승인할 수 있습니다.");
        }

        // 대기 중인 지원만 승인 가능
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidException(24, "대기 중인 지원만 승인할 수 있습니다.");
        }

        // 모집 중인 공고만 매칭 가능
        if (post.getPostStatus() != PostStatus.OPEN) {
            throw new InvalidException(25, "모집 중인 공고만 지원할 수 있습니다.");
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
            throw new InvalidException(26, "이미 매칭된 다른 공고와 동행 시간이 겹칩니다.");
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

        log.info("[지원 수락] applicationId={}, postId={}, escortId={}, clientId={}",
                applicationId, post.getId(), escort.getId(), userId);
        return new ApplicationAcceptResponse(application);
    }

    @Transactional
    public void reject(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(24, "지원을 찾을 수 없습니다."));

        Post post = application.getPost();

        // 본인 공고에 들어온 지원만 거절 가능
        if (!post.getClient().getId().equals(userId)) {
            throw new ForbiddenException(24, "본인 공고의 지원만 거절할 수 있습니다.");
        }

        // 대기 중인 지원만 거절 가능
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidException(27, "대기 중인 지원만 거절할 수 있습니다.");
        }

        application.reject();
        log.info("[지원 거절] applicationId={}, clientId={}", applicationId, userId);
    }

    private boolean isTimeOverlapping(Post firstPost, Post secondPost) {
        return firstPost.getEscortStartAt().isBefore(secondPost.getEscortEndAt())
                && firstPost.getEscortEndAt().isAfter(secondPost.getEscortStartAt());
    }

    @Transactional
    public void cancel(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(24, "지원을 찾을 수 없습니다."));

        // 본인이 지원한 내역만 취소 가능
        if (!application.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(25, "본인이 지원한 내역만 취소할 수 있습니다.");
        }

        // 대기 상태에서 지원 취소
        if (application.getStatus() == ApplicationStatus.PENDING) {
            application.cancel();
            log.info("[지원 취소] applicationId={}, escortId={}", applicationId, userId);
            return;
        }

        // 승인 후 동행인 취소
        if (application.getStatus() == ApplicationStatus.ACCEPTED) {

           // Post post = application.getPost();
            Post post = postRepository.findByIdWithLock(application.getPost().getId())
                    .orElseThrow(() -> new NotFoundException(11, "%d번 공고가 없습니다.".formatted(application.getPost().getId())));

            EscortProfile escortProfile = escortProfileRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(3, "동행 매니저 프로필이 존재하지 않습니다."));

            application.noShow();
            escortProfile.increaseNoShowCount();

            // 노쇼 데이터 생성
            noShowPenaltyService.noShow(application);

            // 모집 마감 전이면 다시 동행인을 모집
            if (LocalDateTime.now().isBefore(post.getRecruitEndAt())) {
                post.reopen();
            } else {
                // 모집 마감 후라면 공고 취소
                post.matchedCancel();
            }
            // 매칭 확정 후 동행인이 취소하면 노쇼로 처리되므로 warn
            log.warn("[노쇼 취소] applicationId={}, postId={}, escortId={}", applicationId, post.getId(), userId);
            return;
        }

        throw new InvalidException(28, "취소할 수 없는 지원 상태입니다.");
    }

    @Transactional
    public void updateProgress(Long applicationId, Long userId, EscortProgress progress) {

        Application application = applicationRepository.findByIdWithLock(applicationId)
                .orElseThrow(() -> new NotFoundException(24, "지원을 찾을 수 없습니다."));

        // 본인의 동행 진행 상태만 변경 가능
        if (!application.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(26, "본인의 동행 진행 상태만 변경할 수 있습니다.");
        }

        // 승인된 지원만 동행 진행 상태 변경 가능
        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new InvalidException(29, "승인된 지원만 동행 진행 상태를 변경할 수 있습니다.");
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
            throw new InvalidException(30, "이미 동행이 완료되었습니다.");
        }

        if (progress != nextProgress) {
            throw new InvalidException(31, "동행 진행 상태를 순서대로 변경해야 합니다.");
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
        log.info("[동행 진행상태 변경] applicationId={}, progress={}, escortId={}", applicationId, progress, userId);
    }

    // 이 지원의 현재 동행 진행 단계 조회. 본인(동행인) 또는 이 공고를 작성한 의뢰인만 볼 수 있다.
    @Transactional(readOnly = true)
    public ApplicationProgressResponse getProgress(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(24, "지원을 찾을 수 없습니다."));

        boolean isEscort = application.getEscort().getId().equals(userId);
        boolean isClient = application.getPost().getClient().getId().equals(userId);
        if (!isEscort && !isClient) {
            throw new ForbiddenException(27, "본인의 동행 건만 진행 상태를 조회할 수 있습니다.");
        }

        EscortProgress current = escortProgressLogRepository
                .findTopByApplicationOrderByOccurredAtDescIdDesc(application)
                .map(EscortProgressLog::getProgress)
                .orElse(EscortProgress.NOT_STARTED);

        return new ApplicationProgressResponse(applicationId, current);
    }

    @Transactional(readOnly = true)
    public ApplicationEscortProfileResponse getEscortProfile(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(24, "지원을 찾을 수 없습니다."));

        Post post = application.getPost();

        // 본인 공고에 지원한 동행인 프로필만 조회 가능
        if (!post.getClient().getId().equals(userId)) {
            throw new ForbiddenException(20, "본인 공고의 지원자 프로필만 조회할 수 있습니다.");
        }

        User escort = application.getEscort();

        EscortProfile escortProfile = escortProfileRepository.findById(escort.getId())
                .orElseThrow(() -> new NotFoundException(3, "동행 매니저 프로필이 존재하지 않습니다."));

        double rating = escortProfile.getRatingCount() == 0
                ? 0.0
                : (double) escortProfile.getRatingSum()
                / escortProfile.getRatingCount();

        int age = Period.between(escort.getBirthDate(), LocalDate.now()).getYears();

        return new ApplicationEscortProfileResponse(
                escort.getId(),
                escort.getName(),
                escortProfile.getVerified(),
                escortProfile.getIntro(),
                escortProfile.getCompletedCount(),
                rating,
                escortProfile.getRatingCount(),
                escortProfile.getNoShowCount(),
                escortProfile.getGrade(),
                age,
                escort.getGender(),
                escort.getRegion()
        );
    }
    @Transactional(readOnly = true)
    public ApplicationClientProfileResponse getClientProfile(Long applicationId, Long userId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(24, "지원을 찾을 수 없습니다."));

        // 본인이 승인받은 지원 건의 의뢰인 정보만 조회 가능
        if (!application.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(28, "매칭된 의뢰인의 프로필만 조회할 수 있습니다.");
        }

        User client = application.getPost().getClient();

        ClientProfile clientProfile = clientProfileRepository.findById(client.getId())
                .orElseThrow(() -> new NotFoundException(2, "의뢰인 프로필이 존재하지 않습니다."));

        return new ApplicationClientProfileResponse(
                client.getId(),
                client.getName(),
                client.getPhoneNum(),
                clientProfile.getEmergencyContactName(),
                clientProfile.getEmergencyContactPhone(),
                clientProfile.getCareNote()
        );
    }
    @Transactional(readOnly = true)
    public List<MyApplicationResponse> getMyApplications(Long userId) {

        User escort = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(1, "회원 정보를 찾을 수 없습니다."));

        if (escort.getRole() != Role.ESCORT) {
            throw new InvalidException(32, "동행 매니저만 지원 내역을 조회할 수 있습니다.");
        }

        return applicationRepository
                .findAllByEscortIdWithPost(userId)
                .stream()
                .map(MyApplicationResponse::new)
                .toList();
    }

    /**
     * 동행 매니저의 현재 위치를 저장한다. 가장 최근 위치 1건만 남고, 보낼 때마다 덮어쓴다.
     * 5초마다 호출되는 API 라서, 엔티티를 불러와 고치지 않고 위치 컬럼만 바꾸는 조건부 UPDATE 를 쓴다.
     * (이유: ApplicationRepository.updateLocation 주석 참고)
     */
    @Transactional
    public void saveLocation(Long applicationId, Long userId, ApplicationLocationRequest request) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(24, "지원을 찾을 수 없습니다."));

        // 본인의 동행에서만 위치를 보낼 수 있다
        if (!application.getEscort().getId().equals(userId)) {
            throw new ForbiddenException(29, "본인이 진행하는 동행에서만 실시간 위치를 보낼 수 있습니다.");
        }

        // 승인된 지원만. (노쇼 등으로 끝난 지원의 동행인이, 같은 공고를 이어받은 다른 동행인의 동행에 위치를 쓰지 못하게)
        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new InvalidException(33, "승인된 동행에서만 실시간 위치를 보낼 수 있습니다.");
        }

        // 동행이 진행 중일 때만 (출발 기록 후 ~ 동행 완료 전)
        if (application.getPost().getPostStatus() != PostStatus.IN_PROGRESS) {
            throw new InvalidException(34, "동행이 진행 중일 때만 실시간 위치를 보낼 수 있습니다.");
        }

        int updated = applicationRepository.updateLocation(
                applicationId, request.lat(), request.lng(), request.accuracy(), Instant.now());

        // 위 확인 이후 저장하기 전에 동행이 끝나거나 취소된 경우 (조건부 UPDATE 가 0건)
        if (updated == 0) {
            throw new InvalidException(34, "동행이 진행 중일 때만 실시간 위치를 보낼 수 있습니다.");
        }
      //  log.info("[실시간 위치 저장] applicationId={}, escortId={}", applicationId, userId);
    }
    @Transactional(readOnly = true)
    public ApplicationLocationResponse getLocation(Long applicationId, Long userId) {
        // TODO: 위치 조회 구현
        //  1. 지원 조회 (없으면 404)
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(24, "지원을 찾을 수 없습니다."));


        // 동행 매니저 본인 또는 그 공고를 작성한 의뢰인만 볼 수 있다 (getProgress 와 같은 기준)
        boolean isEscort = application.getEscort().getId().equals(userId);
        boolean isClient = application.getPost().getClient().getId().equals(userId);
        if (!isEscort && !isClient) {
            throw new ForbiddenException(30, "본인의 동행 건만 실시간 위치를 조회할 수 있습니다.");
        }
        // 동행이 진행 중이 아니면 위치를 돌려주지 않는다.
        // TODO(위치 삭제): 개인정보 처리방침에 "동행 종료 후 지체 없이 삭제"를 명시했다. 아직 미구현이라 GET 에서만 막아 둠.
        //  - ApplicationRepository 에 clearLocation(벌크 UPDATE) 추가
        //  - 호출 지점: updateProgress(ARRIVED_HOME), PostService.escortComplete, 노쇼 취소, matchedCancel
        if (application.getStatus() != ApplicationStatus.ACCEPTED
                || application.getPost().getPostStatus() != PostStatus.IN_PROGRESS) {
            throw new NotFoundException(31, "아직 공유된 실시간 위치가 없습니다.");
        }
        //  저장된 위치가 없으면(application.getLat() == null) NotFoundException → 프론트는 404 를 "아직 없음"으로 처리
        if (application.getLat() == null || application.getLng() == null) {
            throw new NotFoundException(31, "아직 공유된 실시간 위치가 없습니다.");
        }
      //  log.info("[실시간 위치 조회] applicationId={}, userId={}", applicationId, userId);
        return new ApplicationLocationResponse(application);
    }
}
