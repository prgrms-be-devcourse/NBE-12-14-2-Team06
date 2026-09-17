package com.back.nbe12142team06.domain.post.service;

import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.ride.entity.Ride;
import com.back.nbe12142team06.domain.ride.entity.RideDirection;
import com.back.nbe12142team06.domain.ride.repository.RideRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import com.back.nbe12142team06.global.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;

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
    public Post write(Long userId, PostWriteRequest request) {
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

        // 결제 데이터 생성
        createPayment(post);
        // 이동수단 데이터 생성
        createRide(post);

        return postRepository.save(post);
    }

    private void createPayment(Post post) {
        BigDecimal hours = BigDecimal.valueOf(
                Duration.between(post.getEscortStartAt(), post.getEscortEndAt()).toMinutes() / 60);
        Payment payment = Payment.builder()
                .post(post)
                .hourlyPaySnapshot(post.getHourlyPay())
                .hours(hours)
                .amount(hours.multiply(BigDecimal.valueOf(post.getHourlyPay())).intValue())
                .build();
        paymentRepository.save(payment);
    }

    private void createRide(Post post) {
        Ride rideToHos = Ride.builder()
                .post(post)
                .build();
        Ride rideToHome = Ride.builder()
                .post(post)
                .direction(RideDirection.TO_HOME)
                .build();
        rideRepository.save(rideToHos);
        rideRepository.save(rideToHome);
    }

    @Transactional
    public void modify(Long postId, Long userId, PostWriteRequest request) {
        User user = getUser(userId);
        Post post = postRepository.findByIdWithClient(postId)
                .orElseThrow(() -> new NotFoundException(1, postId + "번 공고가 없습니다."));

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException(11, "공고 수정 권한이 없습니다. 의뢰인으로 로그인 해주세요.");
        }
        if (!post.getClient().getId().equals(user.getId())) {
            throw new UnauthorizedException(12, "본인이 작성한 공고만 수정할 수 있습니다.");
        }
        if (!LocalDateTime.now().isBefore(post.getRecruitStartAt())) {
            throw new InvalidException(8, "모집이 시작된 이후에는 공고를 수정할 수 없습니다. 모집 삭제 후 재등록해주세요.");
        }
        if (post.getPostStatus() != PostStatus.OPEN){
            throw new InvalidException(9,"모집 중 상태에서만 수정이 가능합니다.");
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
        ); //더티체킹으로 자동 updatec 쿼리 생성
    }
    @Transactional
    public void delete(Long postId, Long userId) {
        User user = getUser(userId);
        Post post = findById(postId);

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException(13, "공고 삭제 권한이 없습니다.");
        }
        if (!post.getClient().getId().equals(user.getId())) {
            throw new UnauthorizedException(14, "본인이 작성한 공고만 삭제할 수 있습니다.");
        }
        if (post.getPostStatus() != PostStatus.OPEN && post.getPostStatus() != PostStatus.EXPIRED) {
            throw new InvalidException(10, "모집 중이거나 만료 상태에서만 삭제가 가능합니다.");
        }
        postRepository.deleteById(postId);
    }
}