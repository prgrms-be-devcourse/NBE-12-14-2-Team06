package com.back.nbe12142team06.domain.post.scheduler;

import com.back.nbe12142team06.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostScheduler {

    private final PostService postService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void expireOverduePosts() {
        postService.expireOverduePosts();
    }
}