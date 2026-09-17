package com.back.nbe12142team06.domain.review.controller;

import com.back.nbe12142team06.domain.review.dto.ReviewDto;
import com.back.nbe12142team06.domain.review.service.ReviewService;
import com.back.nbe12142team06.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserReviewController {

    private final ReviewService reviewService;

    // 특정 동행인이 받은 리뷰 목록 조회
    // 리뷰는 동행인 프로필에 공개되는 정보이므로 별도 권한 체크 없음
    @GetMapping("/{userId}/reviews")
    public RsData<List<ReviewDto>> list(@PathVariable Long userId) {

        List<ReviewDto> reviews = reviewService.findAllByEscortId(userId)
                .stream()
                .map(ReviewDto::new)
                .toList();

        return new RsData<>("200-1", "리뷰 목록 조회 성공", reviews);
    }
}