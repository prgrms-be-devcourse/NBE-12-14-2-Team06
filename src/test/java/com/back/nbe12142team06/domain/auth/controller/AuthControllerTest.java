package com.back.nbe12142team06.domain.auth.controller;

import com.back.nbe12142team06.domain.auth.entity.RefreshToken;
import com.back.nbe12142team06.domain.auth.repository.RefreshTokenRepository;
import com.back.nbe12142team06.global.security.RefreshTokenGenerator;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;




    @Test
    @DisplayName("[AuthController] 로그인 - 회원가입 한 아이디로 정상 로그인")
    void t1() throws Exception {
        String signUpBody = """
        {
            "username": "user1",
            "password": "pwd1",
            "email": "first@test.test",
            "name": "김춘식",
            "role": "CLIENT",
            "gender": "MALE",
            "birthDate": "1990-05-20",
            "phoneNum": "010-1234-5678",
            "region": "서울시"
        }
        """;

        String body = """
        {
            "username": "user1",
            "password": "pwd1"
        }
        """;

        // 회원 가입
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andDo(print());

        // 로그인
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andDo(
                        print()
                );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("김춘식님 반갑습니다."))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("김춘식"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().path("accessToken", "/"))   // access Token의 요청 Path가 전체인지 검증
                .andExpect(cookie().path("refreshToken", "/api/v1/auth"));  // refresh Token의 요청 Path가 /api/v1/auth/refresh인지 검증
    }

    @Test
    @DisplayName("[AuthController] 로그인 - 존재하지 않는 아이디로 로그인 시도 시 401")
    void t2() throws Exception {
        String body = """
        {
            "username": "user1",
            "password": "pwd1"
        }
        """;

        // 로그인
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andDo(
                        print()
                );

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401"))
                .andExpect(jsonPath("$.msg").value("아이디 또는 비밀번호가 올바르지 않습니다."))
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));
    }


    @Test
    @DisplayName("[AuthController] 로그인 - 아이디는 존재하지만 비밀번호가 옳지 않은 로그인 시도 시 401")
    void t3() throws Exception {
        String signUpBody = """
        {
            "username": "user1",
            "password": "pwd1",
            "email": "first@test.test",
            "name": "김춘식",
            "role": "CLIENT",
            "gender": "MALE",
            "birthDate": "1990-05-20",
            "phoneNum": "010-1234-5678",
            "region": "서울시"
        }
        """;

        String body = """
        {
            "username": "user1",
            "password": "pwd999999999999999999999"
        }
        """;

        // 회원 가입
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andDo(print());

        // 로그인
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andDo(
                        print()
                );

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401"))
                .andExpect(jsonPath("$.msg").value("아이디 또는 비밀번호가 올바르지 않습니다."))
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));
    }

    @Test
    @DisplayName("[AuthController] 로그아웃 - 로그아웃 시 토큰 폐기")
    void t4() throws Exception {
        String signUpBody = """
        {
            "username": "user1",
            "password": "pwd1",
            "email": "first@test.test",
            "name": "김춘식",
            "role": "CLIENT",
            "gender": "MALE",
            "birthDate": "1990-05-20",
            "phoneNum": "010-1234-5678",
            "region": "서울시"
        }
        """;

        // 회원 가입
        MvcResult signUpResult = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andReturn();

        Cookie accessToken = signUpResult.getResponse().getCookie("accessToken");
        Cookie rawRefreshToken = signUpResult.getResponse().getCookie("refreshToken");

        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/auth/logout")
                                .cookie(accessToken)
                                .cookie(rawRefreshToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))
                .andExpect(result -> {

                    // accessToken 폐기 확인
                    Cookie newAccessToken = result.getResponse().getCookie("accessToken");
                    assertThat(newAccessToken.getValue()).isEmpty();
                    assertThat(newAccessToken.getMaxAge()).isEqualTo(0);
                    assertThat(newAccessToken.getPath()).isEqualTo("/");
                    assertThat(newAccessToken.isHttpOnly()).isTrue();

                    // refreshToken 폐기 확인
                    Cookie newRefreshToken = result.getResponse().getCookie("refreshToken");
                    assertThat(newRefreshToken.getValue()).isEmpty();
                    assertThat(newRefreshToken.getMaxAge()).isEqualTo(0);
                    assertThat(newRefreshToken.getPath()).isEqualTo("/api/v1/auth");
                });

        // 캐시 비우고 실제로 DB에서 조회
        em.flush();
        em.clear();

        // DB에서 폐기 확인
        String hash = RefreshTokenGenerator.hash(rawRefreshToken.getValue());
        RefreshToken saved = this.refreshTokenRepository.findByTokenHash(hash).orElseThrow();
        assertThat(saved.isRevoked()).isTrue();
    }
}
