package com.back.nbe12142team06.domain.review.dto;

import com.back.nbe12142team06.domain.review.entity.Review;
import com.back.nbe12142team06.domain.review.entity.ReviewTag;

import java.time.LocalDateTime;
import java.util.Set;

// 리뷰 조회용
public record ReviewDto(
        Long id,
        Long applicationId,
        Integer rating,
        Set<ReviewTag> tags,
        String content,
        LocalDateTime createdAt
) {
    public ReviewDto(Review review) {
        this(
                review.getId(),
                review.getApplication().getId(),
                review.getRating(),
                // 엔티티의 컬렉션을 그대로 들고 나가면 지연 로딩 껍데기를 붙잡게 된다.
                // 트랜잭션이 끝난 뒤 JSON 으로 바꿀 때 세션이 없어 터지므로, 여기서 복사해 끊어낸다.
                Set.copyOf(review.getTags()),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}