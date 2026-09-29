package com.back.nbe12142team06.domain.review.controller;

import com.back.nbe12142team06.domain.review.dto.ReviewWriteRequest;
import com.back.nbe12142team06.domain.review.dto.ReviewWriteResponse;
import com.back.nbe12142team06.domain.review.entity.Review;
import com.back.nbe12142team06.domain.review.service.ReviewService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰", description = "동행 리뷰 작성 및 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "동행 리뷰 작성",
            description = "특정 동행 건에 대한 리뷰를 작성합니다."
    )

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