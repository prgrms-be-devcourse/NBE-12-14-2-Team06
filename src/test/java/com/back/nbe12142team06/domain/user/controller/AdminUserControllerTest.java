package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AdminUserControllerTest {

    private static final String ADMIN_USERNAME = "adminTest";
    private static final String ADMIN_PASSWORD = "adminTest";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 관리자 계정 주입
    private void createTestAdmin() {
        User admin = new User(
                ADMIN_USERNAME,
                passwordEncoder.encode(ADMIN_PASSWORD),
                "admin@admin.admin",
                "관리자",
                Role.ADMIN,
                Gender.MALE,
                LocalDate.of(2001, 1, 1),
                "010-9898-9898",
                "서울시"
        );
        userRepository.save(admin);
    }

    // 로그인 후 accessToken 쿠키 반환
    private Cookie login(String username, String password) throws Exception {
        String loginBody = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        Cookie accessToken = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        assertThat(accessToken).isNotNull();
        return accessToken;
    }

    // 관리자 로그인 (createTestAdmin() 호출 후 사용)
    private Cookie loginAsAdmin() throws Exception {
        return login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    // 일반 회원 가입 후 accessToken 쿠키 반환 (가입 시 토큰이 발급됨)
    private Cookie signUp(String username) throws Exception {
        String signUpBody = """
                {
                    "username": "%s",
                    "password": "testPassword",
                    "email": "%s@user.user",
                    "name": "김춘식",
                    "role": "CLIENT",
                    "gender": "MALE",
                    "birthDate": "1990-05-20",
                    "phoneNum": "%s010-8080-0000",
                    "region": "서울시"
                }
                """.formatted(username, username, username);

        Cookie accessToken = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        assertThat(accessToken).isNotNull();
        return accessToken;
    }

    private Long findUserId(String username) {
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    @Test
    @DisplayName("[AdminUserController] 회원 단건 조회 - 관리자가 정상적으로 단건 조회 시 200-1 반환")
    void t1() throws Exception {
        createTestAdmin();
        signUp("user1");
        Long user1Id = findUserId("user1");
        Cookie adminToken = loginAsAdmin();

        // 회원 단건 조회
        ResultActions resultActions = mvc.perform(
                get("/api/v1/admin/users/{id}", user1Id)
                        .cookie(adminToken)
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("회원 정보 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(user1Id))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("user1@user.user"))
                .andExpect(jsonPath("$.data.name").value("김춘식"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-20"))
                .andExpect(jsonPath("$.data.phoneNum").value("user1010-8080-0000"))
                .andExpect(jsonPath("$.data.region").value("서울시"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.deletedAt").value(nullValue()));
    }

    @Test
    @DisplayName("[AdminUserController] 회원 단건 조회 - 탈퇴한 회원도 관리자는 조회할 수 있고 200-1 반환")
    void t2() throws Exception {
        createTestAdmin();
        Cookie user1Token = signUp("user1");
        Long user1Id = findUserId("user1");   // 탈퇴 후에는 조회되지 않으므로 먼저 확보

        // user1 탈퇴
        mvc.perform(delete("/api/v1/users/profile")
                        .cookie(user1Token))
                .andExpect(status().isNoContent());

        Cookie adminToken = loginAsAdmin();

        // 탈퇴 회원 단건 조회
        ResultActions resultActions = mvc.perform(
                get("/api/v1/admin/users/{id}", user1Id)
                        .cookie(adminToken)
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("회원 정보 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(user1Id))
                .andExpect(jsonPath("$.data.username").value("deleted_%d".formatted(user1Id)))
                .andExpect(jsonPath("$.data.deletedAt").exists());
    }

    @Test
    @DisplayName("[AdminUserController] 회원 단건 조회 - 로그인 없이 조회 시 401-1 반환")
    void t3() throws Exception {
        createTestAdmin();
        signUp("user1");
        Long user1Id = findUserId("user1");

        // 쿠키 없이 조회
        ResultActions resultActions = mvc.perform(
                get("/api/v1/admin/users/{id}", user1Id)
        ).andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("[AdminUserController] 회원 단건 조회 - 일반 회원이 조회 시 403-1 반환")
    void t4() throws Exception {
        Cookie user1Token = signUp("user1");
        Long user1Id = findUserId("user1");

        // 일반 회원 토큰으로 조회 (자기 자신이어도 관리자 API는 불가)
        ResultActions resultActions = mvc.perform(
                get("/api/v1/admin/users/{id}", user1Id)
                        .cookie(user1Token)
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403-1"));
    }

    @Test
    @DisplayName("[AdminUserController] 회원 단건 조회 - 존재하지 않는 회원 조회 시 404 반환")
    void t5() throws Exception {
        createTestAdmin();
        Cookie adminToken = loginAsAdmin();

        // 존재하지 않는 id로 조회
        ResultActions resultActions = mvc.perform(
                get("/api/v1/admin/users/{id}", Long.MAX_VALUE)
                        .cookie(adminToken)
        ).andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg").value("회원 정보를 찾을 수 없습니다."));
    }


    @Test
    @DisplayName("[AdminController] 회원 다건 조회 - 관리자가 정상 조회 시 200 반환")
    void t6() throws Exception {
        // 관리자 로그인
        createTestAdmin();
        Cookie adminToken = loginAsAdmin();

        // 다수의 회원 생성
        for (int i = 1; i < 20; i++){
            signUp("user%d".formatted(i));
        }

        ResultActions resultActions = mvc.perform(
                get("/api/v1/admin/users")
                        .cookie(adminToken)
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.content.length()").value(10))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(20))   // 관리자 1 + 회원 19
                .andExpect(jsonPath("$.data.totalPages").value(2))
                // 최신 가입자가 맨 앞
                .andExpect(jsonPath("$.data.content[0].username").value("user19"))
                .andExpect(jsonPath("$.data.content[9].username").value("user10"));

        // 생성일 내림차순 검증
        String responseBody = resultActions.andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        List<LocalDateTime> createdAts = JsonPath.<List<String>>read(responseBody, "$.data.content[*].createdAt")
                .stream()
                .map(LocalDateTime::parse)
                .toList();

        assertThat(createdAts).isSortedAccordingTo(Comparator.reverseOrder());

    }


    @Test
    @DisplayName("[AdminController] 회원 다건 조회 - 음수 페이지 요청 시 400-1 반환")
    void t7() throws Exception {
        // 관리자 로그인
        createTestAdmin();
        Cookie adminToken = loginAsAdmin();

        // 다수의 회원 생성
        for (int i = 1; i < 4; i++){
            signUp("user%d".formatted(i));
        }

        ResultActions resultActions = mvc.perform(
                get("/api/v1/admin/users")
                        .param("page", "-1")
                        .cookie(adminToken)
        ).andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("페이지 번호는 음수일 수 없습니다."));
    }

    @Test
    @DisplayName("[AdminController] 회원 다건 조회 - 비로그인의 회원 목록 조회 요청 시 401-1 반환")
    void t8() throws Exception {

        // 다수의 회원 생성
        for (int i = 1; i < 4; i++){
            signUp("user%d".formatted(i));
        }

        ResultActions resultActions = mvc.perform(
                get("/api/v1/admin/users")
                        .param("page", "-1")
        ).andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }
}