package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.auth.service.RefreshTokenService;
import com.back.nbe12142team06.domain.user.dto.profile.*;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpResponse;
import com.back.nbe12142team06.domain.user.dto.user.UserProfileUpdateRequest;
import com.back.nbe12142team06.domain.user.dto.user.UserResponse;
import com.back.nbe12142team06.domain.user.entity.ClientProfile;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
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

    // username 중복 검사
    @GetMapping("/username")
    public RsData<Boolean> checkUsername(@RequestParam String username) {
        Boolean isAvailable = this.userService.isUsernameAvailable(username);

        return new RsData<>(
                "200-2",
                isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.",
                isAvailable
        );
    }

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
                "회원 가입이 완료되었습니다.",
                new UserSignUpResponse(createUser)
        );
    }

    // 내 정보 조회
    @GetMapping("/profile")
    public RsData<UserResponse> profile(@AuthenticationPrincipal SecurityUser me) {
        User user = this.userService.myProfile(me.getId());

        return new RsData<>(
                "200-1",
                "내 정보 조회가 완료되었습니다.",
                new UserResponse(user)
        );
    }

    // 회원 정보 수정
    @PatchMapping("/profile")
    public RsData<UserResponse> updateProfile(
            @AuthenticationPrincipal SecurityUser me,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        User user = this.userService.updateMyProfile(me.getId(), request);

        return new RsData<>(
                "200-3",
                "회원 정보가 수정되었습니다.",
                new UserResponse(user)
        );
    }

    // 회원 탈퇴 -> 해당 회원의 모든 리프레시, 억세스 토큰 폐기 처리
    @DeleteMapping("/profile")
    public RsData<Void> deleteProfile(@AuthenticationPrincipal SecurityUser me) {

        // 유저 정보 삭제
        this.userService.deleteMyProfile(me.getId());

        this.rq.clearTokenCookies();

        return new RsData<>(
                "200-4",
                "회원 탈퇴가 완료되었습니다."
        );
    }

    // 의뢰인 프로필 생성
    @PostMapping("/profile/client")
    public RsData<ClientProfileResponse> createProfileClient(
            @AuthenticationPrincipal SecurityUser me,
            @RequestBody @Valid ClientProfileRequest request
    ) {

        ClientProfile clientProfile = this.userService.createClientProfile(me.getId(), request);

        return new RsData<>(
                "200-5",
                "의뢰인 프로필이 생성되었습니다.",
                new ClientProfileResponse(clientProfile)
                );
    }

    @GetMapping("/profile/client")
    public RsData<ClientProfileResponse> getProfileClient(
            @AuthenticationPrincipal SecurityUser me
    ){
        ClientProfile clientProfile = this.userService.getClientProfile(me.getId());

        return new RsData<>(
                "200-6",
                "의뢰인 프로필 조회를 완료했습니다.",
                new ClientProfileResponse(clientProfile)
        );
    }

    // 관리자, 동행인이 프로필 조회 시
    @GetMapping("/{userId}/profile/client")
    public RsData<ClientProfileResponse> getClientProfile(
            @AuthenticationPrincipal SecurityUser me,
            @PathVariable Long userId
    ) {
        ClientProfile clientProfile = this.userService.getClientProfile(me.getId(), userId);

        return new RsData<>(
                "200-6",
                "의뢰인 프로필 조회가 완료되었습니다.",
                new ClientProfileResponse(clientProfile)
        );
    }

    // 의뢰인 자기 자신 프로필 수정
    @PutMapping("/profile/client")
    public RsData<ClientProfileModifyResponse> updateProfileClient(
            @AuthenticationPrincipal SecurityUser me,
            @RequestBody @Valid ClientProfileModifyRequest request
    ){
        ClientProfile clientProfile = this.userService.updateClientProfile(me.getId(), request);

        return new RsData<>(
                "200-7",
                "의뢰인 프로필 수정을 완료했습니다.",
                new ClientProfileModifyResponse(clientProfile)
        );
    }

    // 동행 매니저 프로필 생성
    @PostMapping("/profile/escort")
    public RsData<EscortProfileResponse> createProfileEscort(
            @AuthenticationPrincipal SecurityUser me,
            @RequestBody @Valid EscortProfileRequest request
    ) {

        EscortProfile escort = this.userService.createEscortProfile(me.getId(), request);

        return new RsData<>(
                "200-8",
                "동행 매니저 프로필이 생성되었습니다.",
                new EscortProfileResponse(escort)
        );
    }

    // 동행 매니저 자기 자신 프로필 조회 (계좌 정보 포함)
    @GetMapping("/profile/escort")
    public RsData<EscortProfileResponse> getProfileEscort(
            @AuthenticationPrincipal SecurityUser me
    ){
        EscortProfile escortProfile = this.userService.getEscortProfile(me.getId());

        return new RsData<>(
                "200-9",
                "동행 매니저 프로필 조회를 완료했습니다.",
                new EscortProfileResponse(escortProfile)
        );
    }

    // 의뢰인의 동행 매니저 프로필 조회 (계좌 정보 미포함)
    @GetMapping("/{userId}/profile/escort")
    public RsData<EscortProfileForClientResponse> getProfileEscortByClient(@PathVariable Long userId){
        EscortProfile escortProfile = this.userService.getEscortProfile(userId);

        return new RsData<>(
                "200-9",
                "동행 매니저 프로필 조회를 완료했습니다.",
                new EscortProfileForClientResponse(escortProfile)
        );
    }

    // 동행인 프로필 수정
    @PutMapping("/profile/escort")
    public RsData<EscortProfileModifyResponse> updateProfileEscort(
        @AuthenticationPrincipal SecurityUser me,
        @RequestBody @Valid EscortProfileModifyRequest request
    ){

        EscortProfile escortProfile = this.userService.updateEscortProfile(me.getId(), request);

        return new RsData<>(
                "200-10",
                "동행 매니저 프로필 수정을 완료했습니다.",
                new EscortProfileModifyResponse(escortProfile)
        );
    }

}
