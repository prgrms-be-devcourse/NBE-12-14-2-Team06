package com.back.nbe12142team06.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("관리자"),
    CLIENT("의뢰인"),
    ESCORT("동행인");

    private final String description;

    public List<GrantedAuthority> toAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + name()));
    }
}
