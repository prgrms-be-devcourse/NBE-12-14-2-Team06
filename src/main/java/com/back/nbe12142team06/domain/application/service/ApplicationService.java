package com.back.nbe12142team06.domain.application.service;

import com.back.nbe12142team06.domain.application.dto.ApplicationAcceptResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationApplyResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationListResponse;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.BusinessException;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

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
    public List<ApplicationListResponse> list(Long postId, Long userId) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException("공고를 찾을 수 없습니다."));

        if (!post.getClient().getId().equals(userId)) {
            throw new BusinessException("403-1", "본인 공고의 지원 목록만 조회할 수 있습니다.");
        }

        List<Application> applications = applicationRepository.findAllByPostIdWithEscort(postId);

        return applications.stream()
                .map(ApplicationListResponse::new)
                .toList();
    }

    @Transactional
    public ApplicationAcceptResponse accept(Long applicationId, Long userId){

        Application application = applicationRepository.findById(applicationId).orElseThrow(
                () -> new NotFoundException("지원을 찾을 수 없습니다."));

        Post post = application.getPost();

        // 본인 공고에 들어온 지원만 승인 가능
        if(!post.getClient().getId().equals(userId)){
            throw new BusinessException("403-1", "본인 공고의 지원만 승인할 수 있습니다.");
        }

        // 대기 중인 지원만 승인 가능
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidException("대기 중인 지원만 승인할 수 있습니다.");
        }

        // 모집 중인 공고만 매칭 가능
        if (post.getPostStatus() != PostStatus.OPEN) {
            throw new InvalidException("모집 중인 공고만 매칭할 수 있습니다.");
        }

        application.accept();
        post.match();

        return new ApplicationAcceptResponse(application);
    }
}
