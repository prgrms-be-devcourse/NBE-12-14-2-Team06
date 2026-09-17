package com.back.nbe12142team06.domain.settlement.controller;

import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
