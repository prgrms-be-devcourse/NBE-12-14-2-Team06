package com.back.nbe12142team06.domain.post.dto;

import com.back.nbe12142team06.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostWriteResponse(
        Long id,
        String title,
        String postStatus,
        LocalDateTime createdAt
) {
    public PostWriteResponse(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getPostStatus().getDescription(),
                post.getCreatedAt()
        );
    }
}