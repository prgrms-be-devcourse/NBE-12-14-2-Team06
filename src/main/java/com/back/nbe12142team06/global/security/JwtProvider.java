package com.back.nbe12142team06.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtProvider {
        // 토큰 발급
        public static String toString(String secret, long expireMills, Map<String, Object> body) {
            Claims claims = Jwts.claims()
                    .add(body)
                    .build();

            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + expireMills * 1000L);

            Key secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            String jwt = Jwts.builder()
                    .claims(claims)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(secretKey)
                    .compact();

            return jwt;
        }

        // 검증 후 페이로드 추출
        public static Map<String, Object> payload(String jwt, String secretPattern) {

            SecretKey secretKey = Keys.hmacShaKeyFor(secretPattern.getBytes(StandardCharsets.UTF_8));

            return  Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        }
}



