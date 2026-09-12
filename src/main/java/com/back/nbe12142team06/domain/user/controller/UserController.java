package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpResponse;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public RsData<UserSignUpResponse> signUp(@RequestBody @Valid UserSignUpRequest userSignUpRequest) {

        User createUser = this.userService.signUp(userSignUpRequest.username(), userSignUpRequest.password(), userSignUpRequest.email(), userSignUpRequest.name(), userSignUpRequest.role(), userSignUpRequest.gender(), userSignUpRequest.birthDate(), userSignUpRequest.phoneNum(), userSignUpRequest.region());

        return new RsData<>(
                "201-1",
                "회원가입이 완료되었습니다.",
                new UserSignUpResponse(createUser)
        );
    }

}
