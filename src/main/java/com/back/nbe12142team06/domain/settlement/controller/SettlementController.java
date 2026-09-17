package com.back.nbe12142team06.domain.settlement.controller;

import com.back.nbe12142team06.domain.settlement.dto.SettlementResponse;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    // 정산 목록 기능
    @GetMapping
    public RsData<List<SettlementResponse>> settlementList(@AuthenticationPrincipal SecurityUser actor,
                                                           @RequestParam LocalDate startDate,
                                                           @RequestParam LocalDate endDate) {
        Long userId = actor.getId();

        List<Settlement> settlements = settlementService.findAll(userId, startDate, endDate);

        return new RsData<>("200-31", "정산 목록을 가져왔습니다.",
                settlements.stream().map(SettlementResponse::new).toList());
    }
}
