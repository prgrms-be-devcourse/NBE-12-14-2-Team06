package com.back.nbe12142team06.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // 인증은 됐지만 권한이 부족한 경우 호출 (403)
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {
                    "statusCode": "403-1",
                    "msg": "권한이 없습니다."
                }
                """);

    }
}
