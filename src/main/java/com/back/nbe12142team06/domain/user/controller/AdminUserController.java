package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.user.dto.admin.AdminUserProfileUpdateRequest;
import com.back.nbe12142team06.domain.user.dto.admin.AdminUserResponse;
import com.back.nbe12142team06.domain.user.dto.profile.EscortProfileResponse;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final UserService  userService;

    // [ADMIN] 회원 정보 단건 조회
    @GetMapping("/users/{userId}")
    public RsData<AdminUserResponse> getUser(@PathVariable Long userId) {

        User user = this.userService.findByIdIncludingDeleted(userId);


        return new RsData<>(
                "200-1",
                "회원 정보 조회가 완료되었습니다.",
                new AdminUserResponse(user)
        );
    }


    // [ADMIN] 회원 정보 다건 조회 (페이징처리까지)
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


    // [ADMIN] 회원 정보 수정 username, password는 변경 불가
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

    // [ADMIN] 회원 탈퇴 - 이미 탈퇴한 회원 불가, 자기 자신 탈퇴 불가 (관리자가 한명일 때 대비용)
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

    // [ADMIN] 의뢰인의 동행 매니저 프로필 조회 (계좌 정보 포함)
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
