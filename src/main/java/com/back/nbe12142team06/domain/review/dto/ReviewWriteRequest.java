package com.back.nbe12142team06.domain.review.dto;

import com.back.nbe12142team06.domain.review.entity.ReviewTag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

// 리뷰 작성용
public record ReviewWriteRequest(

        @NotNull(message = "별점은 필수 항목입니다.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer rating,

        @Size(max = 5, message = "태그는 최대 5개까지 선택할 수 있습니다.")
        Set<ReviewTag> tags,      // 선택 입력

        @Size(max = 500, message = "리뷰 내용은 500자 이하여야 합니다.")
        String content            // 선택 입력
) {
}