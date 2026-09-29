package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.global.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${custom.jwt.secret-key}")
    private String secretPattern;

    @Value("${custom.jwt.expire-seconds}")
    private long expireSeconds;

    public String genAccessToken(User user) {
        return JwtProvider.toString(
                secretPattern,
                expireSeconds,
                Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "role", user.getRole().name()
                )
        );
    }

    public Map<String, Object> payload(String jwt) {
        Map<String, Object> payload = JwtProvider.payload(jwt, secretPattern);

        long id = ((Number) payload.get("id")).longValue();
        String username = (String) payload.get("username");
        String role = (String) payload.get("role");

        return Map.of("id", id, "username", username, "role", role);
    }
}
