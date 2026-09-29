package com.back.nbe12142team06.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 인증이 필요한데 SecurityContext가 비어있을 때 호출된다
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String error = (String) request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR);

        String resultCode;
        String msg;


        if (JwtAuthenticationFilter.ERROR_EXPIRED.equals(error)) {  // 만료인 경우
            resultCode = "401-2";
            msg = "만료된 토큰입니다.";
        } else if (JwtAuthenticationFilter.ERROR_INVALID.equals(error)) {   // 서명 불일치, 형식 오류
            resultCode = "401-3";
            msg = "유효하지 않은 토큰입니다.";
        } else {    // 토큰이 없는 경우
            resultCode = "401-1";
            msg = "로그인 후 이용해주세요.";
        }

        write(response, 401, resultCode, msg);
    }

    private void write(HttpServletResponse response, int status,
                       String resultCode, String msg) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("""
                {
                    "statusCode": "%s",
                    "msg": "%s"
                }
                """.formatted(resultCode, msg));
    }
}
