package com.back.nbe12142team06.global.security;

import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.rq.Rq;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR = "authError";
    public static final String ERROR_EXPIRED = "expired";
    public static final String ERROR_INVALID = "invalid_token";

    private final UserService userService;
    private final Rq rq;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
        throws ServletException, IOException {

        // /api로 시작하는 요청이 아니면 전부 통과
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveAccessToken();

        // 토큰 없으면 익명 상태로 통과
        if (accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, Object> payload = this.userService.payload(accessToken);
            setAuthentication(payload);
        } catch (ExpiredJwtException e) {
            request.setAttribute(AUTH_ERROR, ERROR_EXPIRED);    // 토큰이 만료인 경우 (api/v1/auth/refresh로 재발급)
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(AUTH_ERROR, ERROR_INVALID);    // 재로그인이 필요한 경우
        }
        // 인증이 안되었더라도 통과, 어차피 인가 단계에서 잡히고 /auth/refresh 호출을 위해서입니다.
        filterChain.doFilter(request, response);
    }

    // payload에서 꺼낸 값으로 인증 객체 생성 후 SecurityContext에 저장
    private void setAuthentication(Map<String, Object> payload) {
        Long id = ((Number)payload.get("id")).longValue();
        String username = (String) payload.get("username");
        Role role = Role.valueOf((String) payload.get("role"));

        // Role에 맞는 권한 설정
        List<GrantedAuthority> authorities = role.toAuthorities();
        SecurityUser principal = new SecurityUser(id, username, authorities);

        // ContextHolder에 저장
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities)
        );
    }

    private String resolveAccessToken() {
        return rq.getAccessToken();
    }
}
