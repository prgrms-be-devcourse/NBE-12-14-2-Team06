package com.back.nbe12142team06.domain.post.service;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    /* 의존석 객체 */
    private final PostRepository postRepository;

    /* 메서드 */
    public List<Post> findAll() {
        return postRepository.findAll().reversed();
    }
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 공고가 없습니다."));
    }

}
