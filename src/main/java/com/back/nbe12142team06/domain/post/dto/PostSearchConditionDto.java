package com.back.nbe12142team06.domain.post.dto;

import java.time.LocalDateTime;

public record PostSearchConditionDto(
        String keyword,
        String region,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        Integer minPay,
        Integer maxPay,
        boolean openOnly   // true = 모집중 탭(OPEN만), false = 마감 탭(OPEN 아닌 것만)
) {}