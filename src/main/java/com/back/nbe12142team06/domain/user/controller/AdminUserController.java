package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.user.dto.user.AdminUserProfileUpdateRequest;
import com.back.nbe12142team06.domain.user.dto.user.AdminUserResponse;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
                "200",
                "회원 목록 조회가 완료되었습니다.",
                userPage
        );
    }


    // [관리자] 회원 정보 수정
    @PatchMapping("/users/{userId}")
    public RsData<AdminUserResponse> updateProfile(
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
}
