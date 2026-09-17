package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.auth.service.RefreshTokenService;
import com.back.nbe12142team06.domain.user.dto.login.common.UserLoginRequest;
import com.back.nbe12142team06.domain.user.dto.login.common.UserLoginResponse;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpResponse;
import com.back.nbe12142team06.domain.user.dto.user.UserResponse;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.rq.Rq;
import com.back.nbe12142team06.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;

    // 회원가입
    @PostMapping
    public RsData<UserSignUpResponse> signUp(@RequestBody @Valid UserSignUpRequest request) {

        User createUser = this.userService.signUp(request);

        // access token, refresh token 생성
        String accessToken = this.userService.genAccessToken(createUser);
        String refreshToken = this.refreshTokenService.generate(createUser);

        // access token, refresh token 설정
        this.rq.setAccessTokenCookie(accessToken);
        this.rq.setRefreshTokenCookie(refreshToken);

        return new RsData<>(
                "201-1",
                "회원가입이 완료되었습니다.",
                new UserSignUpResponse(createUser)
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

    // 내 정보 조회
    @GetMapping("/profile")
    public RsData<UserResponse> profile(@AuthenticationPrincipal SecurityUser me) {
        User user = this.userService.myProfile(me.getId());

        return new RsData<>(
                "200-1",
                "내 정보 조회가 완료되었습니다",
                new UserResponse(user)
        );
    }

    // username 중복 검사
    @GetMapping
    public RsData<Boolean> checkUsername(@RequestParam String username) {
        Boolean isAvailable = this.userService.isUsernameAvailable(username);

        return new RsData<>(
                "200-2",
                isAvailable ? "사용 가능한 아이디입니다." : "이미 사용중인 아이디입니다.",
                isAvailable
        );
    }
}
