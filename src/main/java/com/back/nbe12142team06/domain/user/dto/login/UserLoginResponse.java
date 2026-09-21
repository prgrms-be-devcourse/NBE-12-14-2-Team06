package com.back.nbe12142team06.domain.user.dto.login;

import com.back.nbe12142team06.domain.user.entity.User;

public record UserLoginResponse(
        Long id,
        String name
) {
    public UserLoginResponse(User user){
        this(
                user.getId(),
                user.getName()
        );
    }
}
