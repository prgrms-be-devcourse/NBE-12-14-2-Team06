package com.back.nbe12142team06.domain.review.controller;

import com.back.nbe12142team06.domain.review.dto.ReviewWriteRequest;
import com.back.nbe12142team06.domain.review.dto.ReviewWriteResponse;
import com.back.nbe12142team06.domain.review.entity.Review;
import com.back.nbe12142team06.domain.review.service.ReviewService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/{applicationId}/reviews")
    public RsData<ReviewWriteResponse> write(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal SecurityUser actor,
            @RequestBody @Valid ReviewWriteRequest request) {

        Review review = reviewService.write(applicationId, actor.getId(), request);

        return new RsData<>(
                "201-1",
                "%d번 동행 건의 리뷰가 등록되었습니다.".formatted(applicationId),
                new ReviewWriteResponse(review)
        );
    }
}