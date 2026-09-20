package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.user.dto.user.AdminUserResponse;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final UserService  userService;

    // [ADMIN] 회원 정보 단건 조회
    @GetMapping("/users/{userId}")
    public RsData<AdminUserResponse> getUser(@PathVariable Long userId) {

        User user = this.userService.findById(userId);


        return new RsData<>(
                "200-1",
                "회원 정보 조회가 완료되었습니다.",
                new AdminUserResponse(user)
        );
    }


    // TODO: [ADMIN] 회원 정보 다건 조회 (페이징처리까지)
}
