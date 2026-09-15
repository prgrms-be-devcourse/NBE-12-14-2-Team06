package com.back.nbe12142team06.global.rq;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class Rq {
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";

    private final HttpServletRequest request;
    private final HttpServletResponse response;


    @Value("${custom.cookie.secure}")
    private boolean secure;

    @Value("${custom.cookie.same-site}")
    private String sameSite;

    @Value("${custom.jwt.expire-seconds}")
    private long accessExpireSeconds;

    @Value("${custom.jwt.refresh-expire-seconds}")
    private long refreshExpireSeconds;

    public void setAccessTokenCookie(String token) {
        addCookie(ACCESS_TOKEN, token, "/", accessExpireSeconds);
    }

    // refresh 쿠키는 path를 재발급 경로로 제한해 일반 요청에 실리지 않게 한다
    public void setRefreshTokenCookie(String token) {
        addCookie(REFRESH_TOKEN, token, REFRESH_PATH, refreshExpireSeconds);
    }

    public String getAccessToken() {
        return getCookieValue(ACCESS_TOKEN, "");
    }

    public String getRefreshToken() {
        return getCookieValue(REFRESH_TOKEN, "");
    }

    public void clearTokenCookies() {
        deleteCookie(ACCESS_TOKEN, "/");
        deleteCookie(REFRESH_TOKEN, REFRESH_PATH);
    }

    // 쿠키 조회
    public String getCookieValue(String name, String defaultValue){
        Cookie[] cookies = request.getCookies();

        // 쿠키가 없다면 기본 값 반환
        if (cookies == null){
            return defaultValue;
        }

        // 이름이 같고 값이 비어있지 않은 첫 쿠키를 반환
        for (Cookie cookie : cookies){
            if (cookie.getName().equals(name) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }

        return defaultValue;
    }

    // 쿠키 생성 path로 전송 범위, maxAgeSeconds로 수명 제어
    public void addCookie(String name, String value, String path, long maxAgeSeconds){
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge((int)maxAgeSeconds);
        cookie.setHttpOnly(true);   // JS로 접근하는 것을 차단하여 XSS 방어
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", sameSite);

        response.addCookie(cookie);
    }

    // 쿠키 삭제 maxAgr를 0으로 설정하여 즉시 만료되도록 쿠키를 삭제
    public void deleteCookie(String name, String path){
        addCookie(name, "", path, 0);
    }
}
