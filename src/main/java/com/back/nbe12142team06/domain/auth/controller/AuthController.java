package com.back.nbe12142team06.domain.auth.controller;

import com.back.nbe12142team06.domain.auth.service.RefreshTokenService;
import com.back.nbe12142team06.domain.user.dto.login.common.UserLoginRequest;
import com.back.nbe12142team06.domain.user.dto.login.common.UserLoginResponse;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final Rq rq;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;


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

    // 로그인
    @PostMapping("/login")
    public RsData<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        User user = this.userService.login(request);

        String accessToken = this.userService.genAccessToken(user);
        String refreshToken = this.refreshTokenService.generate(user);

        this.rq.setAccessTokenCookie(accessToken);
        this.rq.setRefreshTokenCookie(refreshToken);

        return new RsData<>(
                "200-1",
                "%s님 반갑습니다.".formatted(user.getName()),
                new UserLoginResponse(user)
        );
    }

    // 로그아웃
    @DeleteMapping("/logout")
    public RsData<Void> logout() {

        this.refreshTokenService.revoke(this.rq.getRefreshToken());

        this.rq.clearTokenCookies();

        return new RsData<>(
                "200-3",
                "로그아웃 되었습니다."
        );
    }
}
