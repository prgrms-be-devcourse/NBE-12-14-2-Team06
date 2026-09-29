package com.back.nbe12142team06.domain.auth.controller;

import com.back.nbe12142team06.domain.auth.service.RefreshTokenService;
import com.back.nbe12142team06.domain.user.dto.login.UserLoginRequest;
import com.back.nbe12142team06.domain.user.dto.login.UserLoginResponse;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인, 로그아웃 및 토큰 재발급 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final Rq rq;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 이용해 새로운 Access Token을 발급합니다.")
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

    @Operation(summary = "로그인", description = "사용자 정보를 확인한 후 Access Token과 Refresh Token을 발급합니다.")
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

    @Operation(summary = "로그아웃", description = "Refresh Token을 폐기하고 인증 토큰 쿠키를 삭제합니다.")
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
