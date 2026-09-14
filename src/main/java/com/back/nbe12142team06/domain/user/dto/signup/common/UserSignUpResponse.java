package com.back.nbe12142team06.domain.user.dto.signup.common;

import com.back.nbe12142team06.domain.user.entity.User;

public record UserSignUpResponse(
    Long id,
    String name
) {
    public UserSignUpResponse(User user){
        this(
                user.getId(),
                user.getName()
        );
    }
}
