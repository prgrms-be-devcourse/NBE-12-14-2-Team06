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
import com.back.nbe12142team06.domain.ride.service.RideService;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import com.back.nbe12142team06.global.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public Post findById(Long postId) {
        return postRepository.findByIdWithClient(postId)
                .orElseThrow(() -> new NotFoundException(1, postId + "번 공고가 없습니다."));
    }
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(2, "유저가 없습니다."));
    }
    // 시간 검증
    private void validateTime(LocalDateTime recruitStartAt, LocalDateTime recruitEndAt,
                              LocalDateTime escortStartAt, LocalDateTime escortEndAt) {
        if (!LocalDateTime.now().isBefore(recruitStartAt)) {
            throw new InvalidException(4, "모집 시작 시간은 현재 시간보다 이후여야 합니다.");
        }
        if (!recruitStartAt.isBefore(recruitEndAt)) {
            throw new InvalidException(7, "모집 시작 시간은 모집 마감 시간보다 빨라야 합니다.");
        }
        if (!recruitEndAt.isBefore(escortStartAt)) {
            throw new InvalidException(3, "모집 마감 시간은 동행 시작 시간보다 빨라야 합니다.");
        }
        if (!escortStartAt.isBefore(escortEndAt)) {
            throw new InvalidException(2, "동행 시작 시간은 종료 시간보다 빨라야 합니다.");
        }
    }

    @Transactional
    public PostWriteResponse write(Long userId, PostWriteRequest request) {
        User user = getUser(userId);
        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException(10, "공고 등록 권한이 없습니다. 의뢰인으로 로그인 해주세요.");
        }
        validateTime(request.recruitStartAt(), request.recruitEndAt(),
                request.escortStartAt(), request.escortEndAt());

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
        rideService.createRide(savedPost);

        return new PostWriteResponse(savedPost, payment.getId());
    }

    @Transactional
    public void modify(Long postId, Long userId, PostWriteRequest request) {
        User user = getUser(userId);
        Post post = postRepository.findByIdWithClient(postId)
                .orElseThrow(() -> new NotFoundException(1, postId + "번 공고가 없습니다."));

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException(11, "공고 수정 권한이 없습니다. 의뢰인으로 로그인 해주세요.");
        }
        if (user.getRole() == Role.CLIENT) {
            if (!post.getClient().getId().equals(user.getId())) {
                throw new UnauthorizedException(12, "본인이 작성한 공고만 수정할 수 있습니다.");
            }
            if (!LocalDateTime.now().isBefore(post.getRecruitStartAt())) {
                throw new InvalidException(8, "모집이 시작된 이후에는 공고를 수정할 수 없습니다. 모집 삭제 후 재등록해주세요.");
            }
            if (post.getPostStatus() != PostStatus.OPEN){
                throw new InvalidException(9,"모집 중 상태에서만 수정이 가능합니다.");
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
    }
    @Transactional
    public void delete(Long postId, Long userId) {
        User user = getUser(userId);
        Post post = findById(postId);

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException(13, "공고 삭제 권한이 없습니다.");
        }
        if (user.getRole() == Role.CLIENT) {
            if (!post.getClient().getId().equals(user.getId())) {
                throw new UnauthorizedException(14, "본인이 작성한 공고만 삭제할 수 있습니다.");
            }
            if (post.getPostStatus() != PostStatus.OPEN && post.getPostStatus() != PostStatus.EXPIRED) {
                throw new InvalidException(10, "모집 중이거나 만료 상태에서만 삭제가 가능합니다.");
            }
        }
        postRepository.deleteById(postId);
    }
    @Transactional
    public void matchedCancel(Long postId, Long userId) {
        User user = getUser(userId);
        Post post = findById(postId);

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException(15, "공고 취소 권한이 없습니다.");
        }

        // 의뢰인만 소유자/상태 검증. 관리자는 무조건 통과.
        if (user.getRole() == Role.CLIENT) {
            if (!post.getClient().getId().equals(user.getId())) {
                throw new UnauthorizedException(16, "본인이 작성한 공고만 취소할 수 있습니다.");
            }
            if (post.getPostStatus() != PostStatus.MATCHED) {
                throw new InvalidException(13, "매칭된 상태에서만 취소할 수 있습니다.");
            }
        }

        post.matchedCancel();
    }
    @Transactional
    public void expireOverduePosts() {
        List<Post> targets = postRepository.findAllByPostStatusAndRecruitEndAtBefore(
                PostStatus.OPEN, LocalDateTime.now());

        targets.forEach(post -> post.expire());
        // 변경 감지(더티체킹)로 트랜잭션 끝날 때 자동으로 UPDATE 쿼리 나감
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
            throw new UnauthorizedException(15, "공고 동행완료 처리 권한이 없습니다.");
        }

        // 의뢰인만 소유자/상태 검증. 관리자는 무조건 통과.
        if (user.getRole() == Role.CLIENT) {
            if (!post.getClient().getId().equals(user.getId())) {
                throw new UnauthorizedException(16, "본인이 작성한 공고만 동행완료 처리할 수 있습니다.");
            }
            if (post.getPostStatus() != PostStatus.IN_PROGRESS) {
                throw new InvalidException(13, "동행진행중 상태에서만 동행완료 처리할 수 있습니다.");
            }
        }
        // 이 공고의 매칭된(승인된) 지원 조회
        Application application = applicationRepository.findAllByPostAndStatus(post, ApplicationStatus.ACCEPTED)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(17, "매칭된 지원 내역이 없습니다."));

        // 출발(DEPARTED) 시각 조회
        LocalDateTime departedAt = escortProgressLogRepository
                .findByApplicationAndProgress(application, EscortProgress.DEPARTED)
                .map(EscortProgressLog::getOccurredAt)
                .orElseThrow(() -> new NotFoundException(18, "동행 출발 기록이 없습니다."));

        // 귀가완료(ARRIVED_HOME) 시각 조회
        LocalDateTime arrivedAt = escortProgressLogRepository
                .findByApplicationAndProgress(application, EscortProgress.ARRIVED_HOME)
                .map(EscortProgressLog::getOccurredAt)
                .orElseThrow(() -> new NotFoundException(19, "귀가완료 기록이 없습니다."));

        post.startProgress(departedAt);   // escortStartAt 실제값 반영
        post.complete(arrivedAt);       // escortEndAt 실제값 반영 + 상태 COMPLETED

        // 동행인 프로필의 동행 완료 건수 증가
        EscortProfile escortProfile = this.escortProfileRepository.findById(application.getEscort().getId())
                .orElseThrow(() -> new NotFoundException(20, "동행 매니저 프로필이 존재하지 않습니다."));
        escortProfile.increaseCompletedCount();

        // 재결제 로직
        paymentService.validPayment(userId, post, application, post.getEscortEndAt().plusDays(1).toLocalDate());
    }
}