package com.back.nbe12142team06.domain.review.dto;

import com.back.nbe12142team06.domain.review.entity.Review;
import com.back.nbe12142team06.domain.review.entity.ReviewTag;

import java.time.LocalDateTime;
import java.util.Set;

public record ReviewWriteResponse(
        Long id,
        Long applicationId,
        Integer rating,
        Set<ReviewTag> tags,
        String content,
        LocalDateTime createdAt
) {
    public ReviewWriteResponse(Review review) {
        this(
                review.getId(),
                review.getApplication().getId(),
                review.getRating(),
                review.getTags(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}