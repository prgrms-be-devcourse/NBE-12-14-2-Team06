package com.back.nbe12142team06.domain.user.controller;

import com.back.nbe12142team06.global.security.JwtProvider;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${custom.jwt.secret-key}")
    private String secretKey;

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
                .andExpect(jsonPath("$.msg").value("회원가입이 완료되었습니다."))
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

        // 같은 username, 다른 email로 가입 시도
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
                .andExpect(cookie().path("refreshToken", "/api/v1/auth/refresh"));  // refresh Token의 요청 Path가 /api/v1/auth/refresh인지 검증
    }


    @Test
    @DisplayName("[UserController] 로그인 - 회원가입 한 아이디로 정상 로그인")
    void t8() throws Exception {
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
                post("/api/v1/users/login")
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
                .andExpect(cookie().path("refreshToken", "/api/v1/auth/refresh"));  // refresh Token의 요청 Path가 /api/v1/auth/refresh인지 검증
    }

    @Test
    @DisplayName("[UserController] 로그인 - 존재하지 않는 아이디로 로그인 시도 시 401")
    void t9() throws Exception {
        String body = """
        {
            "username": "user1",
            "password": "pwd1"
        }
        """;

        // 로그인
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/users/login")
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
    @DisplayName("[UserController] 로그인 - 아이디는 존재하지만 비밀번호가 옳지 않은 로그인 시도 시 401")
    void t10() throws Exception {
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
                        post("/api/v1/users/login")
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
                        post("/api/v1/users/login")
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
                .andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다"));
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
                get("/api/v1/users/username?username=user1")
        )
        .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-2"))
                .andExpect(jsonPath("$.msg").value("사용 가능한 아이디입니다."))
                .andExpect(jsonPath("$.data").value("true"));
    }
}
