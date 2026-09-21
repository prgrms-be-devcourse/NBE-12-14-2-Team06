package com.back.nbe12142team06.domain.user.dto.profile;

import com.back.nbe12142team06.domain.user.entity.User;

public record ClientProfileResponse(
        Long userId,
        String name
) {
    public ClientProfileResponse(User user){
        this(
                user.getId(),
                user.getName()
        );
    }
}
