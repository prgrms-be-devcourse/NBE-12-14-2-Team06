package com.back.nbe12142team06.domain.review.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.review.dto.ReviewWriteRequest;
import com.back.nbe12142team06.domain.review.entity.Review;
import com.back.nbe12142team06.domain.review.entity.ReviewTag;
import com.back.nbe12142team06.domain.review.repository.ReviewRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용 트랜잭션으로 설정
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EscortProfileRepository escortProfileRepository;

    @Transactional // 쓰기 작업을 수행하는 메서드에는 readOnly 해제
    public Review write(Long applicationId, Long actorId, ReviewWriteRequest request) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(1, "존재하지 않는 동행 건입니다."));

        // 리뷰는 해당 공고를 등록한 의뢰인만 작성 가능
        if (!application.getPost().getClient().getId().equals(actorId)) {
            throw new ForbiddenException(1, "본인이 의뢰한 동행 건에만 리뷰를 작성할 수 있습니다.");
        }

        // 매칭이 확정된 동행 건에만 리뷰 작성 가능
        // || application.getPost().getPostStatus() != PostStatus.COMPLETED 의뢰인이 노쇼 후 리뷰 작성 방지용으로 제안드립니다.
        if (application.getStatus() != ApplicationStatus.ACCEPTED || application.getPost().getPostStatus() != PostStatus.COMPLETED) {
            throw new InvalidException(1, "매칭이 확정된 동행 건에만 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByApplicationId(applicationId)) {
            throw new DuplicatedException(1, "이미 리뷰가 작성된 동행 건입니다.");
        }

        // 태그 미선택 시 null 이 들어오므로 빈 컬렉션으로 대체
        Set<ReviewTag> tags = request.tags() == null ? new HashSet<>() : request.tags();

        Review review = reviewRepository.save(
                Review.builder()
                        .application(application)
                        .rating(request.rating())
                        .tags(tags)
                        .content(request.content())
                        .build()
        );

        // 동행 매니저 프로필 평점 반영
        EscortProfile escortProfile = this.escortProfileRepository.findById(application.getEscort().getId())
                .orElseThrow(() -> new NotFoundException(3, "동행 매니저 프로필이 존재하지 않습니다."));
        escortProfile.addRating(request.rating());

        return review;
    }

    // 특정 동행인이 받은 리뷰 목록 조회 (클래스의 readOnly 적용)
    public List<Review> findAllByEscortId(Long escortId) {

        // 리뷰가 0건인 것은 정상이므로, 회원 존재 여부만 확인
        if (!userRepository.existsById(escortId)) {
            throw new NotFoundException(2, "존재하지 않는 회원입니다.");
        }

        return reviewRepository.findAllByEscortIdWithApplication(escortId);
    }
}