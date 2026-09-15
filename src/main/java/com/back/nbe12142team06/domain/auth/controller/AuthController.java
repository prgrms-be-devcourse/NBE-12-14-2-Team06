package com.back.nbe12142team06.domain.auth.controller;

import com.back.nbe12142team06.domain.auth.service.RefreshTokenService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final Rq rq;
    private final RefreshTokenService refreshTokenService;


    @PostMapping("/refresh")
    public RsData<Void> refresh(){
        String rawRefreshToken = this.rq.getRefreshToken();
        String newAccessToken = this.refreshTokenService.refresh(rawRefreshToken);

        this.rq.setAccessTokenCookie(newAccessToken);

        return new RsData<>(
                "200-1",
                "토큰 재발급되었습니다."
                );
    }
}
