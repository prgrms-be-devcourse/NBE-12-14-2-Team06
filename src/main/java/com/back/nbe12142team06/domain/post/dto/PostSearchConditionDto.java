package com.back.nbe12142team06.domain.post.dto;

import java.time.LocalDateTime;

public record PostSearchConditionDto(
        String keyword,
        String region,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        Integer minPay,
        Integer maxPay
) {}