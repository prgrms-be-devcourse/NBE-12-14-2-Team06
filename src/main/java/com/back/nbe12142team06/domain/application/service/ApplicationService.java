package com.back.nbe12142team06.domain.application.service;

import com.back.nbe12142team06.domain.application.dto.ApplicationApplyResponse;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApplicationApplyResponse apply(Long postId, String username) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException("공고를 찾을 수 없습니다."));

        // 모집 중인 공고만 지원 가능
        if (post.getPostStatus() != PostStatus.OPEN) {
            throw new InvalidException("모집 중인 공고에만 지원할 수 있습니다.");
        }

        User escort = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        // 동행인만 지원 가능
        if (escort.getRole() != Role.ESCORT) {
            throw new InvalidException("동행인만 공고에 지원할 수 있습니다.");
        }

        // 동일 공고 중복 지원 방지
        if(applicationRepository.existsByPostAndEscort(post, escort)){
            throw new DuplicatedException("이미 지원한 공고입니다.");
        }

        Application application = Application.builder()
                .post(post)
                .escort(escort)
                .build();

        Application savedApplication = applicationRepository.save(application);
        return new ApplicationApplyResponse(savedApplication);
    }
}
