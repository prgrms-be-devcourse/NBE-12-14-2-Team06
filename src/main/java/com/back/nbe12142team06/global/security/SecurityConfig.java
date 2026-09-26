package com.back.nbe12142team06.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")   // 관리자
                    .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()  // 회원가입
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/username").permitAll()  // username 중복 검사
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()  // 로그인
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()   // access 토큰 재발급
                    // swagger
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    // 프로필
                    .requestMatchers("/api/v1/users/*/profile/client").hasAnyRole("ADMIN", "ESCORT")    // 의뢰인 프로필 타인 조회, 수정
                    .requestMatchers("/api/v1/users/profile/client").hasRole("CLIENT") // 의뢰인 프로필 생성, 조회, 수정
                    .requestMatchers("/api/v1/users/*/profile/escort").hasAnyRole("CLIENT")    // 동행인 프로필 타인 조회
                    .requestMatchers("/api/v1/users/profile/escort").hasRole("ESCORT") // 동행인 프로필 생성, 조회, 수정
                    // 교육영상
                    .requestMatchers("/api/v1/education-videos/**").hasRole("ESCORT")  // 교육 영상 목록, 단건, 시청 기록
                    // 공고
                    .requestMatchers(HttpMethod.GET, "/api/v1/posts", "/api/v1/posts/**").permitAll() //공고
                    .requestMatchers(HttpMethod.POST,"/api/v1/posts").hasAnyRole("CLIENT","ADMIN") //공고 생성
                    .requestMatchers(HttpMethod.PUT, "/api/v1/posts/*").hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/*").hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/posts/*/matchedCancel", "/api/v1/posts/*/escortComplete")
                            .hasAnyRole("CLIENT", "ADMIN")

                    // 지원(동행인): 지원하기, 내 지원 목록, 지원 취소, 동행 진행상태 변경
                    .requestMatchers(HttpMethod.POST, "/api/v1/applications/*").hasRole("ESCORT")
                    .requestMatchers(HttpMethod.GET, "/api/v1/applications/me").hasRole("ESCORT")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/applications/*/cancel", "/api/v1/applications/*/progress")
                            .hasRole("ESCORT")

                    // 지원(의뢰인): 내 공고의 지원자 목록, 지원 수락/거절, 지원자 프로필 조회
                    .requestMatchers(HttpMethod.GET, "/api/v1/applications/posts/*").hasRole("CLIENT")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/applications/*/accept", "/api/v1/applications/*/reject")
                            .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.GET, "/api/v1/applications/*/escort-profile").hasRole("CLIENT")

                    // 보고서(작성: 동행인 / 조회: 해당 동행 건의 의뢰인·동행인), 리뷰 작성(의뢰인)
                    .requestMatchers(HttpMethod.POST, "/api/v1/applications/*/report").hasRole("ESCORT")
                    .requestMatchers(HttpMethod.GET, "/api/v1/applications/*/report").hasAnyRole("CLIENT", "ESCORT")
                    .requestMatchers(HttpMethod.POST, "/api/v1/applications/*/reviews").hasRole("CLIENT")

                    // 결제(공고 작성자인 의뢰인), 정산(동행 매니저)
                    .requestMatchers("/api/v1/payments/**").hasRole("CLIENT")
                    .requestMatchers("/api/v1/settlements/**").hasRole("ESCORT")

                    // 위에서 안 걸린 나머지 API 는 로그인만 하면 통과 (역할 무관)
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(e -> e
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            );



        return http.build();
    }
}
