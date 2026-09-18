package com.back.nbe12142team06.domain.settlement.controller;

import com.back.nbe12142team06.domain.settlement.dto.SettlementResponse;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    // 정산 요청 기능
    @PostMapping("/{settlementId}")
    public RsData<?> settlementRequest(@AuthenticationPrincipal SecurityUser actor,
                                       @PathVariable Long settlementId) {
        Long userId = actor.getId();

        settlementService.request(userId, settlementId);

        return new RsData<>("200-30", "정산에 성공했습니다.");
    }

    // 정산 목록 조회 기능
    @GetMapping
    public RsData<Page<SettlementResponse>> settlementList(@AuthenticationPrincipal SecurityUser actor,
                                                           @RequestParam(required = false) LocalDateTime startDate,
                                                           @RequestParam(required = false) LocalDateTime endDate,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "DESC") Sort.Direction sort) {
        Long userId = actor.getId();

        startDate = startDate == null ? LocalDateTime.now().minusMonths(1) : startDate;
        endDate = endDate == null ? LocalDateTime.now() : endDate;

        Page<Settlement> settlements = settlementService.findAll(userId, startDate, endDate, PageRequest.of(page, size,
                Sort.by(sort, "application.post.escortStartAt")));

        return new RsData<>("200-31", "정산 목록을 가져왔습니다.",
                settlements.map(SettlementResponse::new));
    }

    // 정산 조회 기능
    @GetMapping("/{settlementId}")
    public RsData<SettlementResponse> settlementDetail(@AuthenticationPrincipal SecurityUser actor,
                                                       @PathVariable Long settlementId) {

        Long userId = actor.getId();

        Settlement settlement = settlementService.findSettlement(userId, settlementId);

        return new RsData<>("200-32", "정산 상세 데이터를 조회했습니다.",
                new SettlementResponse(settlement));
    }
}
