package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.domain.auth.entity.RefreshToken;
import com.back.nbe12142team06.domain.auth.repository.RefreshTokenRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.security.JwtProvider;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    private static final String ADMIN_USERNAME = "adminTest";
    private static final String ADMIN_PASSWORD = "adminTest";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager em;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${custom.jwt.secret-key}")
    private String secretKey;

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
    @DisplayName("[UserController] 회원가입 -  정상 가입")
    void t1() throws Exception {
        String username = "testUsername";
        String password = "testPassword";
        String email = "testEmail@test.test";
        String name = "김춘식";
        String role = "CLIENT";
        String gender = "MALE";
        String birthDate = "1990-05-20";
        String phoneNum = "010-1234-5678";
        String region = "서울시";

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "%s",
                                            "password": "%s",
                                            "email": "%s",
                                            "name": "%s",
                                            "role": "%s",
                                            "gender": "%s",
                                            "birthDate": "%s",
                                            "phoneNum": "%s",
                                            "region": "%s"
                                        }
                                        """.formatted(username, password, email, name, role, gender, birthDate, phoneNum, region))
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("signUp"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("회원 가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value(name))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }


    @Test
    @DisplayName("[UserController] 회원가입 - 중복된 아이디로 가입 시 409 반환")
    void t2() throws Exception {
        String body = """
            {
                "username": "testUsername",
                "password": "testPassword",
                "email": "%s",
                "name": "김춘식",
                "role": "CLIENT",
                "gender": "MALE",
                "birthDate": "1990-05-20",
                "phoneNum": "%s",
                "region": "서울시"
            }
            """;

        // 첫 번째 가입 성공
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("first@test.test", "010-1234-5678")))
                .andExpect(status().isCreated());

        // 같은 username, 다른 email로 가입 시도
        ResultActions resultActions = mvc
                .perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("second@test.test", "010-1734-5478")))
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-1"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 아이디입니다."));
    }

    @Test
    @DisplayName("[UserController] 회원가입 - 중복된 이메일로 가입 시 409 반환")
    void t3() throws Exception {
        String body = """
            {
                "username": "%s",
                "password": "testPassword",
                "email": "testEmail@test.test",
                "name": "김춘식",
                "role": "CLIENT",
                "gender": "MALE",
                "birthDate": "1990-05-20",
                "phoneNum": "%s",
                "region": "서울시"
            }
            """;

        // 첫 번째 가입 성공
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("user1", "010-1234-5678")))
                .andExpect(status().isCreated());

        // 같은 email, 다른 username으로 가입 시도
        ResultActions resultActions = mvc
                .perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("user2", "010-1234-9999")))
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-2"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("[UserController] 회원가입 - 중복된 전화번호로 가입 시 409 반환")
    void t4() throws Exception {
        String body = """
            {
                "username": "%s",
                "password": "testPassword",
                "email": "%s",
                "name": "김춘식",
                "role": "CLIENT",
                "gender": "MALE",
                "birthDate": "1990-05-20",
                "phoneNum": "010-1234-5678",
                "region": "서울시"
            }
            """;

        // 첫 번째 가입 성공
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("user1", "first@test.test")))
                .andExpect(status().isCreated());

        // 다른 username, 다른 email, 같은 전화번호로 가입 시도
        ResultActions resultActions = mvc
                .perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("user2", "second@test.test")))
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-3"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 전화번호입니다."));
    }

    @Test
    @DisplayName("[UserController] 회원가입 - 필수값 누락 시 400-1 반환")
    void t5() throws Exception {
        // "username": "" 요청
        ResultActions resultActions = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "username": "",
                            "password": "testPassword",
                            "email": "testEmail@test.test",
                            "name": "김춘식",
                            "role": "CLIENT",
                            "gender": "MALE",
                            "birthDate": "1990-05-20",
                            "phoneNum": "010-1234-5678",
                            "region": "서울시"
                        }
                        """))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("username: 아이디는 필수 항목입니다."));
    }


    @Test
    @DisplayName("[UserController] 회원가입 - 존재하지 않는 gender 값으로 요청 시 400-2 반환")
    void t6() throws Exception {
        // "gender": "HELICOPTER"  요청
        ResultActions resultActions = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "username": "user",
                            "password": "testPassword",
                            "email": "testEmail@test.test",
                            "name": "김춘식",
                            "role": "CLIENT",
                            "gender": "HELICOPTER",
                            "birthDate": "1990-05-20",
                            "phoneNum": "010-1234-5678",
                            "region": "서울시"
                        }
                        """))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-2"));
    }


    @Test
    @DisplayName("[UserController] 회원가입 - 회원가입 시 쿠키 발급")
    void t7() throws Exception {
        String body = """
        {
            "username": "testUsername",
            "password": "testPassword",
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
        ResultActions resultActions = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())    // 201 검증
                .andExpect(cookie().exists("accessToken"))  // accessToken이 왔는지 검증
                .andExpect(cookie().exists("refreshToken")) // refreshToken이 왔는지 검증
                .andExpect(cookie().httpOnly("accessToken", true))  // js의 쿠키 접근 차단 검증
                .andExpect(cookie().path("accessToken", "/"))   // access Token의 요청 Path가 전체인지 검증
                .andExpect(cookie().path("refreshToken", "/api/v1/auth"));  // refresh Token의 요청 Path가 /api/v1/auth/refresh인지 검증
    }



    @Test
    @DisplayName("[UserController] 내 정보 조회 - 존재하는 아이디로 정상 로그인 후 내 정보 조회 요청")
    void t11() throws Exception {
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
        Cookie accessToken = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        ResultActions resultActions = mvc.perform(
                        get("/api/v1/users/profile")
                                .cookie(accessToken)
                )
                .andDo(
                        print()
                );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("first@test.test"))
                .andExpect(jsonPath("$.data.name").value("김춘식"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-20"))
                .andExpect(jsonPath("$.data.phoneNum").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.region").value("서울시"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("[UserController] 내 정보 조회 - 로그인 시도 없이 내 정보 조회 요청")
    void t12() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/users/profile")
                )
                .andDo(
                        print()
                );

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("[UserController] 내 정보 조회 - 위조된 토큰으로 요청 시 401-3")
    void t14() throws Exception {
        String validToken = JwtProvider.toString(
                secretKey, 600,
                Map.of("id", 1L, "username", "user1", "role", "CLIENT")
        );

        // 서명 부분의 마지막 글자를 변경
        String forged = validToken.substring(0, validToken.length() - 1)
                + (validToken.endsWith("A") ? "B" : "A");

        ResultActions resultActions = mvc.perform(
                        get("/api/v1/users/profile")
                                .cookie(new Cookie("accessToken", forged))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-3"))
                .andExpect(jsonPath("$.msg").value("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("[UserController] Username 중복 검사 - 사용 가능한 username은 true")
    void t15() throws Exception {
        ResultActions resultActions = mvc.perform(
                get("/api/v1/users/username")
                        .param("username", "user1")
        )
        .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-2"))
                .andExpect(jsonPath("$.msg").value("사용 가능한 아이디입니다."))
                .andExpect(jsonPath("$.data").value("true"));
    }

    @Test
    @DisplayName("[UserController] Username 중복 검사 - 이미 존재하는 username은 false")
    void t16() throws Exception {
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
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andDo(print());

        ResultActions resultActions = mvc.perform(
                        get("/api/v1/users/username")
                                .param("username", "user1")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-2"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 아이디입니다."))
                .andExpect(jsonPath("$.data").value("false"));
    }


    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 정상 수정은 200-3 반환")
    void t18() throws Exception {
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

        String updateBody = """
        {
            "password": "pwd123",
            "email": "firssst@test.test",
            "name": "김춘자",
            "birthDate": "1990-05-29",
            "phoneNum": "010-1334-5678",
            "region": "경기도"
        }
        """;

        // 회원 가입
        MvcResult signUpResult = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andReturn();

        Cookie accessToken = signUpResult.getResponse().getCookie("accessToken");

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("회원 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("firssst@test.test"))
                .andExpect(jsonPath("$.data.name").value("김춘자"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-29"))
                .andExpect(jsonPath("$.data.phoneNum").value("010-1334-5678"))
                .andExpect(jsonPath("$.data.region").value("경기도"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());

        em.flush();
        em.clear();

        User updated = this.userRepository.findByUsername("user1").orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("firssst@test.test");
        assertThat(updated.getName()).isEqualTo("김춘자");
        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 29));
        assertThat(passwordEncoder.matches("pwd1", updated.getPassword())).isFalse();
        assertThat(updated.getPhoneNum()).isEqualTo("010-1334-5678");
        assertThat(updated.getRegion()).isEqualTo("경기도");
        assertThat(passwordEncoder.matches("pwd123", updated.getPassword())).isTrue();  // 비밀번호를 수정 대상에 둔다면
    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 정상 수정은 200-3 반환 - 지역만 수정")
    void t19() throws Exception {
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

        String updateBody = """
        {
            "password": "pwd1",
            "email": "first@test.test",
            "name": "김춘식",
            "birthDate": "1990-05-20",
            "phoneNum": "010-1234-5678",
            "region": "서울시이이이"
        }
        """;

        // 회원 가입
        MvcResult signUpResult = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andReturn();

        Cookie accessToken = signUpResult.getResponse().getCookie("accessToken");

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("회원 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("first@test.test"))
                .andExpect(jsonPath("$.data.name").value("김춘식"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-20"))
                .andExpect(jsonPath("$.data.phoneNum").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.region").value("서울시이이이"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 정상 수정은 200-3 반환 - 전화번호만 수정")
    void t20() throws Exception {
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

        String updateBody = """
        {
            "password": "pwd1",
            "email": "first@test.test",
            "name": "김춘식",
            "birthDate": "1990-05-20",
            "phoneNum": "010-3333-5678",
            "region": "서울시"
        }
        """;

        // 회원 가입
        MvcResult signUpResult = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andReturn();

        Cookie accessToken = signUpResult.getResponse().getCookie("accessToken");

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("회원 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("first@test.test"))
                .andExpect(jsonPath("$.data.name").value("김춘식"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-20"))
                .andExpect(jsonPath("$.data.phoneNum").value("010-3333-5678"))
                .andExpect(jsonPath("$.data.region").value("서울시"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 정상 수정은 200-3 반환 - 생일만 수정")
    void t21() throws Exception {
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

        String updateBody = """
        {
            "password": "pwd1",
            "email": "first@test.test",
            "name": "김춘식",
            "birthDate": "1990-05-29",
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

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("회원 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("first@test.test"))
                .andExpect(jsonPath("$.data.name").value("김춘식"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-29"))
                .andExpect(jsonPath("$.data.phoneNum").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.region").value("서울시"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 정상 수정은 200-3 반환 - 이름만 수정")
    void t22() throws Exception {
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

        String updateBody = """
        {
            "password": "pwd1",
            "email": "first@test.test",
            "name": "김춘자",
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

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("회원 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("first@test.test"))
                .andExpect(jsonPath("$.data.name").value("김춘자"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-20"))
                .andExpect(jsonPath("$.data.phoneNum").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.region").value("서울시"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 정상 수정은 200-3 반환 - 이메일만 수정")
    void t23() throws Exception {
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

        String updateBody = """
        {
            "password": "pwd1",
            "email": "huhuhuhuh@test.test",
            "name": "김춘식",
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

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("회원 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("huhuhuhuh@test.test"))
                .andExpect(jsonPath("$.data.name").value("김춘식"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-20"))
                .andExpect(jsonPath("$.data.phoneNum").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.region").value("서울시"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 정상 수정은 200-3 반환 - 비밀번호만 수정")
    void t24() throws Exception {
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

        String updateBody = """
        {
            "password": "pwd12222222222222222222",
            "email": "first@test.test",
            "name": "김춘식",
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

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("회원 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("first@test.test"))
                .andExpect(jsonPath("$.data.name").value("김춘식"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-20"))
                .andExpect(jsonPath("$.data.phoneNum").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.region").value("서울시"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());

        em.flush();
        em.clear();

        User updated = this.userRepository.findByUsername("user1").orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("first@test.test");
        assertThat(updated.getName()).isEqualTo("김춘식");
        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 20));
        assertThat(passwordEncoder.matches("pwd12222222222222222222", updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 존재하는 email로 수정 시도 시 409-2")
    void t25() throws Exception {
        String signUp1Body = """
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

        String signUp2Body = """
        {
            "username": "user13",
            "password": "pwd1",
            "email": "first1@test.test",
            "name": "김춘식",
            "role": "CLIENT",
            "gender": "MALE",
            "birthDate": "1990-05-20",
            "phoneNum": "010-3333-5678",
            "region": "서울시"
        }
        """;

        String updateBody = """
        {
            "password": "pwd1222",
            "email": "first@test.test",
            "name": "김춘식22",
            "birthDate": "1990-05-25",
            "phoneNum": "010-3333-5678",
            "region": "서울시"
        }
        """;

        // 회원 가입
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUp1Body))
                .andReturn();

        MvcResult signUp2Result = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUp2Body))
                .andReturn();

        Cookie accessToken = signUp2Result.getResponse().getCookie("accessToken");

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-2"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 이메일입니다."));


    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 존재하는 전화번호로 수정 시도 시 409-3")
    void t26() throws Exception {
        String signUp1Body = """
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

        String signUp2Body = """
        {
            "username": "user13",
            "password": "pwd1",
            "email": "first1@test.test",
            "name": "김춘식",
            "role": "CLIENT",
            "gender": "MALE",
            "birthDate": "1990-05-20",
            "phoneNum": "010-3333-5678",
            "region": "서울시"
        }
        """;

        String updateBody = """
        {
            "password": "pwd1222",
            "email": "first3@test.test",
            "name": "김춘식22",
            "birthDate": "1990-05-25",
            "phoneNum": "010-1234-5678",
            "region": "서울시"
        }
        """;

        // 회원 가입
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUp1Body))
                .andReturn();

        MvcResult signUp2Result = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUp2Body))
                .andReturn();

        Cookie accessToken = signUp2Result.getResponse().getCookie("accessToken");

        // 회원 정보 수정
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-3"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 전화번호입니다."));


    }

    @Test
    @DisplayName("[UserController] 회원 탈퇴 - 자기 자신의 계정을 정상적으로 탈퇴하는 경우 200-4 반환")
    void t27() throws Exception {
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
        MvcResult signupResult = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andReturn();

        Cookie accessToken = signupResult.getResponse().getCookie("accessToken");
        Long userId = userRepository.findByUsername("user1").orElseThrow().getId();

        // 회원 탈퇴
        ResultActions resultActions = mvc.perform(
                delete("/api/v1/users/profile")
                        .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-4"))
                .andExpect(jsonPath("$.msg").value("회원 탈퇴가 완료되었습니다."))
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

        em.flush();
        em.clear();

        User deletedUser = (User) em.createNativeQuery(
                        "SELECT * FROM users WHERE id = :id", User.class)
                .setParameter("id", userId)
                .getSingleResult();

        assertThat(deletedUser.getEmail()).isEqualTo("deleted_%d".formatted(userId));
        assertThat(deletedUser.getUsername()).isEqualTo("deleted_%d".formatted(userId));
        assertThat(deletedUser.getPhoneNum()).isEqualTo("deleted_%d".formatted(userId));
        assertThat(deletedUser.getDeletedAt()).isNotNull();

        List<RefreshToken> refreshTokenList = this.refreshTokenRepository.findAllByUserId(userId);

        assertThat(refreshTokenList).isNotEmpty();

        for  (RefreshToken refreshToken : refreshTokenList) {
            assertThat(refreshToken.isRevoked()).isTrue();
        }
    }

    @Test
    @DisplayName("[UserController] 회원 탈퇴 - 탈퇴 후 기존 accessToken으로 요청 시 401")
    void t28() throws Exception {
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
                .andExpect(status().isCreated())
                .andReturn();

        // 탈취된 토큰이라고 가정하고 미리 보관
        Cookie accessToken = signUpResult.getResponse().getCookie("accessToken");

        // 회원 탈퇴
        mvc.perform(delete("/api/v1/users/profile").cookie(accessToken))
                .andExpect(status().isOk());

        // 실제 서버처럼 다음 요청이 새 영속성 컨텍스트에서 시작되도록
        em.flush();
        em.clear();

        // 탈퇴 전에 받은 토큰으로 다시 요청
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/users/profile")
                                .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("[UserController] 회원 탈퇴 - 탈퇴 후 같은 아이디, 이메일, 전화번호로 재가입 가능")
    void t29() throws Exception {
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
                .andExpect(status().isCreated())
                .andReturn();

        Cookie accessToken = signUpResult.getResponse().getCookie("accessToken");

        // 회원 탈퇴
        mvc.perform(delete("/api/v1/users/profile").cookie(accessToken))
                .andExpect(status().isOk());

        em.flush();
        em.clear();

        // 같은 정보로 재가입
        ResultActions resultActions = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-1"));
    }

    @Test
    @DisplayName("[UserController] 회원가입 - 대소문자만 다른 이메일로 가입 시 409-2")
    void t30() throws Exception {
        String body = """
        {
            "username": "%s",
            "password": "testPassword",
            "email": "%s",
            "name": "김춘식",
            "role": "CLIENT",
            "gender": "MALE",
            "birthDate": "1990-05-20",
            "phoneNum": "%s",
            "region": "서울시"
        }
        """;

        // 첫 번째 가입
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("user1", "first@test.test", "010-1234-5678")))
                .andExpect(status().isCreated());

        // 대문자로 바꾼 같은 이메일로 가입 시도
        ResultActions resultActions = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("user2", "FIRST@TEST.TEST", "010-9999-5678")))
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-2"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("[UserController] 회원 정보 수정 - 대소문자만 다른 이메일로 수정 시 409-2")
    void t31() throws Exception {
        String signUpBody = """
        {
            "username": "%s",
            "password": "pwd1",
            "email": "%s",
            "name": "김춘식",
            "role": "CLIENT",
            "gender": "MALE",
            "birthDate": "1990-05-20",
            "phoneNum": "%s",
            "region": "서울시"
        }
        """;

        String updateBody = """
        {
            "password": "pwd1",
            "email": "FIRST@TEST.TEST",
            "name": "김춘식",
            "birthDate": "1990-05-20",
            "phoneNum": "010-3333-5678",
            "region": "서울시"
        }
        """;

        // user1 가입
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody.formatted("user1", "first@test.test", "010-1234-5678")))
                .andExpect(status().isCreated());

        // user2 가입
        MvcResult signUp2Result = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody.formatted("user2", "second@test.test", "010-3333-5678")))
                .andExpect(status().isCreated())
                .andReturn();

        Cookie accessToken = signUp2Result.getResponse().getCookie("accessToken");

        // user2가 user1의 이메일을 대문자로 바꿔서 수정 시도
        ResultActions resultActions = mvc.perform(
                        patch("/api/v1/users/profile")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-2"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("[UserController] 회원 탈퇴 - 로그인 없이 탈퇴 요청 시 401-1")
    void t32() throws Exception {
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/users/profile")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("[UserController] 프로필 생성 - 의뢰인의 정상 프로필 생성 시 200-5 반환")
    void t33() throws Exception {
        Cookie userToken = signUp("user1");

        Long userId = findUserId("user1");

        String createProfileBody = """
                {
                    "emergencyContactName": "김철수",
                    "emergencyContactPhone": "010-1234-5678",
                    "careNote": "여기 아프고 저기 아프고 레전드 아픔. 혼자 보행 불가합니다."
                }
                """;


        ResultActions resultActions = mvc.perform(
                        post("/api/v1/users/profile/client")
                                .cookie(userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createProfileBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-5"))
                .andExpect(jsonPath("$.msg").value("의뢰인 프로필이 생성되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId));

    }

    @Test
    @DisplayName("[UserController] 프로필 생성 - 특이사항이 null이여도 정상 생성")
    void t34() throws Exception {
        Cookie userToken = signUp("user1");

        Long userId = findUserId("user1");

        String createProfileBody = """
                {
                    "emergencyContactName": "김철수",
                    "emergencyContactPhone": "010-1234-5678",
                    "careNote": null
                }
                """;


        ResultActions resultActions = mvc.perform(
                        post("/api/v1/users/profile/client")
                                .cookie(userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createProfileBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-5"))
                .andExpect(jsonPath("$.msg").value("의뢰인 프로필이 생성되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId));

    }

    @Test
    @DisplayName("[UserController] 프로필 생성 - 특이사항이 공백이여도 정상 생성")
    void t35() throws Exception {
        Cookie userToken = signUp("user1");

        Long userId = findUserId("user1");

        String createProfileBody = """
                {
                    "emergencyContactName": "김철수",
                    "emergencyContactPhone": "010-1234-5678",
                    "careNote": ""
                }
                """;


        ResultActions resultActions = mvc.perform(
                        post("/api/v1/users/profile/client")
                                .cookie(userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createProfileBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-5"))
                .andExpect(jsonPath("$.msg").value("의뢰인 프로필이 생성되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId));

    }
}
