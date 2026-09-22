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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "회원가입, 회원 정보 및 의뢰인·동행자 프로필 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;

    @Operation(
            summary = "아이디 중복 검사",
            description = "입력한 아이디의 사용 가능 여부를 확인합니다."
    )
    @GetMapping("/username")
    public RsData<Boolean> checkUsername(@RequestParam String username) {
        Boolean isAvailable = this.userService.isUsernameAvailable(username);

        return new RsData<>(
                "200-2",
                isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.",
                isAvailable
        );
    }

    @Operation(
            summary = "회원가입",
            description = "회원가입 후 Access Token과 Refresh Token을 발급하여 쿠키에 저장합니다."
    )
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

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 회원 정보를 조회합니다."
    )
    @GetMapping("/profile")
    public RsData<UserResponse> profile(@AuthenticationPrincipal SecurityUser me) {
        User user = this.userService.myProfile(me.getId());

        return new RsData<>(
                "200-1",
                "내 정보 조회가 완료되었습니다.",
                new UserResponse(user)
        );
    }

    @Operation(
            summary = "내 정보 수정",
            description = "현재 로그인한 사용자의 회원 정보를 수정합니다."
    )
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

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 사용자의 프로필과 회원 정보를 삭제하고 Refresh Token을 폐기한 뒤 인증 토큰 쿠키를 제거합니다."
    )
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

    @Operation(
            summary = "의뢰인 프로필 생성",
            description = "현재 로그인한 사용자의 의뢰인 프로필을 생성합니다."
    )
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

    @Operation(
            summary = "내 의뢰인 프로필 조회",
            description = "현재 로그인한 사용자의 의뢰인 프로필을 조회합니다."
    )
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

    @Operation(
            summary = "의뢰인 프로필 조회",
            description = "관리자 또는 해당 의뢰인과 매칭된 동행자가 의뢰인 프로필을 조회합니다."
    )
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

    @Operation(
            summary = "의뢰인 프로필 수정",
            description = "현재 로그인한 사용자의 의뢰인 프로필을 수정합니다."
    )
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

    @Operation(
            summary = "동행자 프로필 생성",
            description = "현재 로그인한 사용자의 동행자 프로필을 생성합니다."
    )
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

    @Operation(
            summary = "내 동행자 프로필 조회",
            description = "현재 로그인한 사용자의 동행자 프로필을 계좌 정보를 포함하여 조회합니다."
    )
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

    @Operation(
            summary = "동행자 프로필 조회",
            description = "특정 동행자의 프로필을 계좌 정보를 제외하고 조회합니다."
    )
    @GetMapping("/{userId}/profile/escort")
    public RsData<EscortProfileForClientResponse> getProfileEscortByClient(@PathVariable Long userId){
        EscortProfile escortProfile = this.userService.getEscortProfile(userId);

        return new RsData<>(
                "200-9",
                "동행 매니저 프로필 조회를 완료했습니다.",
                new EscortProfileForClientResponse(escortProfile)
        );
    }


}
