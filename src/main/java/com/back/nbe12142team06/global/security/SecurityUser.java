package com.back.nbe12142team06.global.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {
    private final Long id;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getPassword() {
        return null;
    }
}
