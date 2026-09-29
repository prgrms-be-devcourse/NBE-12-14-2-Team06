package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.user.dto.admin.AdminUserProfileUpdateRequest;
import com.back.nbe12142team06.domain.user.dto.admin.AdminUserResponse;
import com.back.nbe12142team06.domain.user.dto.profile.EscortProfileResponse;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 회원", description = "관리자 회원 조회, 수정, 탈퇴 및 프로필 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final UserService  userService;

    @Operation(
            summary = "회원 상세 조회",
            description = "관리자가 특정 회원의 정보를 조회합니다. 탈퇴한 회원도 조회할 수 있습니다."
    )
    @GetMapping("/users/{userId}")
    public RsData<AdminUserResponse> getUser(@PathVariable Long userId) {

        User user = this.userService.findByIdIncludingDeleted(userId);


        return new RsData<>(
                "200-1",
                "회원 정보 조회가 완료되었습니다.",
                new AdminUserResponse(user)
        );
    }


    @Operation(
            summary = "회원 목록 조회",
            description = "관리자가 탈퇴한 회원을 포함한 회원 목록을 페이지 단위로 조회합니다."
    )
    @GetMapping("/users")
    public RsData<Page<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<User> userEntityPage = this.userService.findAllUsersIncludingDeleted(page, size);
        Page<AdminUserResponse> userPage = userEntityPage.map(AdminUserResponse::new);

        return new RsData<>(
                "200-2",
                "회원 목록 조회가 완료되었습니다.",
                userPage
        );
    }


    @Operation(
            summary = "회원 정보 수정",
            description = "관리자가 특정 회원의 정보를 수정합니다. 탈퇴한 회원은 수정할 수 없으며 아이디와 비밀번호는 변경되지 않습니다."
    )
    @PatchMapping("/users/{userId}")
    public RsData<AdminUserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProfileUpdateRequest request
    ) {
        User user = this.userService.updateUserByAdmin(userId, request);

        return new RsData<>(
                "200-3",
                "회원 정보가 수정되었습니다.",
                new AdminUserResponse(user)
        );
    }

    @Operation(
            summary = "회원 탈퇴 처리",
            description = "관리자가 특정 회원을 탈퇴 처리합니다. 관리자 본인 또는 이미 탈퇴한 회원은 탈퇴 처리할 수 없습니다."
    )
    @DeleteMapping("/users/{id}")
    public RsData<Void> deleteUser(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long id
    ) {
        this.userService.deleteUser(admin.getId(), id);


        return new RsData<>(
                "200-4",
                "회원 탈퇴가 완료되었습니다."
        );
    }

    @Operation(
            summary = "동행자 프로필 조회",
            description = "관리자가 특정 동행자의 프로필을 계좌 정보를 포함하여 조회합니다."
    )
    @GetMapping("/users/{userId}/profile/escort")
    public RsData<EscortProfileResponse> getProfileEscortByAdmin(@PathVariable Long userId){
        EscortProfile escortProfile = this.userService.getEscortProfile(userId);

        return new RsData<>(
                "200-9",
                "동행 매니저 프로필 조회를 완료했습니다.",
                new EscortProfileResponse(escortProfile)
        );
    }
}
