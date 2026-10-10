package com.back.nbe12142team06.domain.post.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.entity.EscortProgressLog;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.repository.EscortProgressLogRepository;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.service.PaymentService;
import com.back.nbe12142team06.domain.post.dto.PostSearchConditionDto;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.dto.PostWriteResponse;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.ride.dto.RideUpdateRequest;
import com.back.nbe12142team06.domain.ride.service.RideService;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import com.back.nbe12142team06.global.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EscortProgressLogRepository escortProgressLogRepository;
    private final ApplicationRepository applicationRepository;
    private final EscortProfileRepository escortProfileRepository;
    private final PaymentService paymentService;
    private final RideService rideService;

    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAllWithClient(pageable);
    }

    // 내가 작성한 공고 목록 — 결제 상태와 무관하게 본인 소유 공고를 전부 보여준다(GET /api/v1/posts 와 달리 결제 DONE 필터 없음).
    public Page<Post> findMyPosts(Long userId, Pageable pageable) {
        return postRepository.findAllByClientId(userId, pageable);
    }

    public Post findById(Long postId) {
        return postRepository.findByIdWithClient(postId)
                .orElseThrow(() -> new NotFoundException(11, postId + "번 공고가 없습니다."));
    }
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(1, "회원 정보를 찾을 수 없습니다."));
    }
    // 시간 검증
    private void validateTime(LocalDateTime recruitStartAt, LocalDateTime recruitEndAt,
                              LocalDateTime escortStartAt, LocalDateTime escortEndAt) {
        if (!LocalDateTime.now().isBefore(recruitStartAt)) {
            throw new InvalidException(11, "모집 시작 시간은 현재 시간보다 이후여야 합니다.");
        }
        if (!recruitStartAt.isBefore(recruitEndAt)) {
            throw new InvalidException(12, "모집 시작 시간은 모집 마감 시간보다 빨라야 합니다.");
        }
        if (!recruitEndAt.isBefore(escortStartAt)) {
            throw new InvalidException(13, "모집 마감 시간은 동행 시작 시간보다 빨라야 합니다.");
        }
        if (!escortStartAt.isBefore(escortEndAt)) {
            throw new InvalidException(14, "동행 시작 시간은 종료 시간보다 빨라야 합니다.");
        }
    }

    @Transactional
    public PostWriteResponse write(Long userId, PostWriteRequest request) {
        User user = getUser(userId);
        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException(11, "공고 등록 권한이 없습니다. 의뢰인으로 로그인 해주세요.");
        }
        validateTime(request.recruitStartAt(), request.recruitEndAt(),
                request.escortStartAt(), request.escortEndAt());

        // 미결제 검증
        boolean isNotPaid = paymentService.validNotPaid(userId);
        if (!isNotPaid) {
            throw new InvalidException(20, "미결제 공고가 있습니다.");
        }

        Post post = Post.builder()
                .client(user)
                .title(request.title())
                .content(request.content())
                .region(request.region())
                .hospitalName(request.hospitalName())
                .hospitalAddress(request.hospitalAddress())
                .hospitalLat(request.hospitalLat())
                .hospitalLng(request.hospitalLng())
                .pickupAddress(request.pickupAddress())
                .pickupLat(request.pickupLat())
                .pickupLng(request.pickupLng())
                .hourlyPay(request.hourlyPay())
                .recruitStartAt(request.recruitStartAt())
                .recruitEndAt(request.recruitEndAt())
                .escortStartAt(request.escortStartAt())
                .escortEndAt(request.escortEndAt())
                .patientNote(request.patientNote())
                .reportRequired(request.reportRequired())
                .build();

        Post savedPost = postRepository.save(post);

        // 결제 데이터 생성
        Payment payment = paymentService.createPayment(savedPost);
        // 이동수단 데이터 생성
        rideService.createRide(savedPost, request.rideSelectToHospital(), request.rideSelectToHome());

        log.info("[공고 등록] postId={}, clientId={}", savedPost.getId(), userId);
        return new PostWriteResponse(savedPost, payment.getId());
    }

    @Transactional
    public void modify(Long postId, Long userId, PostWriteRequest request) {
        User user = getUser(userId);
        Post post = postRepository.findByIdWithClient(postId)
                .orElseThrow(() -> new NotFoundException(1, postId + "번 공고가 없습니다."));

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException(12, "공고 수정 권한이 없습니다. 의뢰인으로 로그인 해주세요.");
        }
        if (user.getRole() == Role.CLIENT) {
            if (!post.getClient().getId().equals(user.getId())) {
                throw new ForbiddenException(13, "본인이 작성한 공고만 수정할 수 있습니다.");
            }
            if (!LocalDateTime.now().isBefore(post.getRecruitStartAt())) {
                throw new InvalidException(15, "모집이 시작된 이후에는 공고를 수정할 수 없습니다. 모집 삭제 후 재등록해주세요.");
            }
            if (post.getPostStatus() != PostStatus.OPEN){
                throw new InvalidException(16, "모집 중 상태에서만 수정이 가능합니다.");
            }
        }

        validateTime(request.recruitStartAt(), request.recruitEndAt(),
                request.escortStartAt(), request.escortEndAt());

        post.modify(
                request.title(), request.content(), request.region(),
                request.hospitalName(), request.hospitalAddress(),
                request.hospitalLat(), request.hospitalLng(),
                request.pickupAddress(), request.pickupLat(), request.pickupLng(),
                request.hourlyPay(),
                request.recruitStartAt(), request.recruitEndAt(),
                request.escortStartAt(), request.escortEndAt(),
                request.patientNote(), request.reportRequired()
        ); //더티체킹으로 자동 update 쿼리 생성

        // 이동 수단 변경
        rideService.updateRide(postId, new RideUpdateRequest(request.rideSelectToHospital(), request.rideSelectToHome()));

        // 수정된 금액과 결제 금액 검증
        paymentService.validPayment(post);

        log.info("[공고 수정] postId={}, userId={}", postId, userId);
    }
    @Transactional
    public void delete(Long postId, Long userId) {
        User user = getUser(userId);
        Post post = findById(postId);

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException(14, "공고 삭제 권한이 없습니다.");
        }
        if (user.getRole() == Role.CLIENT) {
            if (!post.getClient().getId().equals(user.getId())) {
                throw new ForbiddenException(15, "본인이 작성한 공고만 삭제할 수 있습니다.");
            }
            if (post.getPostStatus() != PostStatus.OPEN && post.getPostStatus() != PostStatus.EXPIRED) {
                throw new InvalidException(17, "모집 중이거나 만료 상태에서만 삭제가 가능합니다.");
            }
        }

        // 결제 상태 변경 -> 스케줄러로 삭제된 공고에 대해 결제 취소할 예정
        paymentService.cancelPostAndPayment(postId);

        postRepository.deleteById(postId);
        log.info("[공고 삭제] postId={}, userId={}", postId, userId);
    }
    @Transactional
    public void matchedCancel(Long postId, Long userId) {
        User user = getUser(userId);
        //Post post = findById(postId);
        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(() -> new NotFoundException(11, postId + "번 공고가 없습니다."));

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException(16, "매칭 취소 권한이 없습니다.");
        }

        // 의뢰인만 소유자/상태 검증. 관리자는 무조건 통과.
        if (user.getRole() == Role.CLIENT) {
            if (!post.getClient().getId().equals(user.getId())) {
                throw new ForbiddenException(17, "본인이 작성한 공고만 매칭을 취소할 수 있습니다.");
            }
            if (post.getPostStatus() != PostStatus.MATCHED) {
                throw new InvalidException(18, "매칭된 상태에서만 취소할 수 있습니다.");
            }
        }

        post.matchedCancel();
        log.info("[공고 매칭 취소] postId={}, userId={}", postId, userId);
    }
    @Transactional
    public void expireOverduePosts() {
        List<Post> targets = postRepository.findAllByPostStatusAndRecruitEndAtBefore(
                PostStatus.OPEN, LocalDateTime.now());

        targets.forEach(post -> post.expire());
        // 변경 감지(더티체킹)로 트랜잭션 끝날 때 자동으로 UPDATE 쿼리 나감

        // 1분마다 도는 스케줄러라서 처리한 공고가 있을 때만 남긴다(안 그러면 하루 1,440줄이 쌓임)
        if (!targets.isEmpty()) {
            log.info("[공고 만료 처리] {}건, postIds={}", targets.size(), targets.stream().map(Post::getId).toList());
        }
    }

    public Page<Post> search(PostSearchConditionDto condition, Pageable pageable) {
        return postRepository.search(
                condition.keyword(), condition.region(),
                condition.dateFrom(), condition.dateTo(),
                condition.minPay(), condition.maxPay(),
                condition.openOnly(),
                pageable
        );
    }
    //escortComplete:동행완료가 되면, 에스코트 시작,종료 update되게
    @Transactional
    public void escortComplete(Long postId, Long userId) {
        User user = getUser(userId);
        Post post = findById(postId);

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException(18, "공고 동행완료 처리 권한이 없습니다.");
        }

        // 의뢰인만 소유자/상태 검증. 관리자는 무조건 통과.
        if (user.getRole() == Role.CLIENT) {
            if (!post.getClient().getId().equals(user.getId())) {
                throw new ForbiddenException(19, "본인이 작성한 공고만 동행완료 처리할 수 있습니다.");
            }
            if (post.getPostStatus() != PostStatus.IN_PROGRESS) {
                throw new InvalidException(19, "동행진행중 상태에서만 동행완료 처리할 수 있습니다.");
            }
        }
        // 이 공고의 매칭된(승인된) 지원 조회
        Application application = applicationRepository.findAllByPostAndStatus(post, ApplicationStatus.ACCEPTED)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(21, "매칭된 지원 내역이 없습니다."));

        // 출발(DEPARTED) 시각 조회
        LocalDateTime departedAt = escortProgressLogRepository
                .findByApplicationAndProgress(application, EscortProgress.DEPARTED)
                .map(EscortProgressLog::getOccurredAt)
                .orElseThrow(() -> new NotFoundException(22, "동행 출발 기록이 없습니다."));

        // 귀가완료(ARRIVED_HOME) 시각 조회
        LocalDateTime arrivedAt = escortProgressLogRepository
                .findByApplicationAndProgress(application, EscortProgress.ARRIVED_HOME)
                .map(EscortProgressLog::getOccurredAt)
                .orElseThrow(() -> new NotFoundException(23, "귀가완료 기록이 없습니다."));

        // 동행 매니저 프로필 존재 확인 — 외부 결제 API(validPayment)보다 먼저 검증한다.
        // validPayment 는 Toss 부분 취소를 호출하므로, 그 뒤에 실패하면 환불은 되돌릴 수 없다.
        Long escortId = application.getEscort().getId();
        if (!escortProfileRepository.existsById(escortId)) {
            throw new NotFoundException(3, "동행 매니저 프로필이 존재하지 않습니다.");
        }

        post.startProgress(departedAt);   // escortStartAt 실제값 반영
        post.complete(arrivedAt);       // escortEndAt 실제값 반영 + 상태 COMPLETED

        // 재결제 로직
        paymentService.validPayment(post, application, post.getEscortEndAt().plusDays(1).toLocalDate());

        // 동행 완료 건수 증가 — clearAutomatically 때문에 반드시 마지막에 호출
        int updated = escortProfileRepository.increaseCompletedCount(escortId);
        if (updated == 0) {
            throw new NotFoundException(3, "동행 매니저 프로필이 존재하지 않습니다.");
        }

        log.info("[동행 완료] postId={}, escortId={}, userId={}", postId, escortId, userId);
    }
}