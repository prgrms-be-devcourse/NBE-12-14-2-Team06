package com.back.nbe12142team06.domain.user.controller;

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
                "phoneNum": "010-1234-5678",
                "region": "서울시"
            }
            """;

        // 첫 번째 가입 성공
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("first@test.test")))
                .andExpect(status().isCreated());

        // 같은 username, 다른 email로 가입 시도
        ResultActions resultActions = mvc
                .perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("second@test.test")))
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
                "phoneNum": "010-1234-5678",
                "region": "서울시"
            }
            """;

        // 첫 번째 가입 성공
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("user1")))
                .andExpect(status().isCreated());

        // 같은 email, 다른 username으로 가입 시도
        ResultActions resultActions = mvc
                .perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("user2")))
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-2"))
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("[UserController] 회원가입 - 필수값 누락 시 400-1 반환")
    void t4() throws Exception {
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
    void t5() throws Exception {
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
}
