package com.back.nbe12142team06.domain.ride.controller;

import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.dto.PostWriteResponse;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.ride.entity.RideSelect;
import com.back.nbe12142team06.domain.ride.service.RideService;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class RideControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private RideService rideService;

    private Long savedUser1Id;
    private Long savedUser2Id;
    private Long savedRide1Id;
    private Long savedRide2Id;
    private Long savedPost1Id;
    private Long savedPost2Id;
    private Cookie accessTokenCookie1;
    private Cookie accessTokenCookie2;

    @BeforeEach
    void init() throws Exception {
        String username = "testUsername";
        String password = "testPassword";
        String email = "testEmail@test.test";
        String name = "김춘식";
        String role = "CLIENT";
        String gender = "MALE";
        String birthDate = "1990-05-20";
        String phoneNum = "010-1234-5678";
        String region = "서울시";

        User user1 = userService.signUp(new UserSignUpRequest(
                username, password, email, name, Role.valueOf(role),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                phoneNum, region));

        User user2 = userService.signUp(new UserSignUpRequest(
                username + "2", password + "2", email + "2", name + "2", Role.ESCORT,
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                phoneNum+"2", region));

        String title = "정형외과 동행 구합니다";
        String content = "무릎 수술 후 검진 예약이 있어 동행인이 필요합니다.";
        String postRegion = "서울";
        String hospitalName = "서울성모병원";
        String hospitalAddress = "서울 서초구 반포대로 222";
        BigDecimal hospitalLat = BigDecimal.valueOf(37.5012743);
        BigDecimal hospitalLng = BigDecimal.valueOf(127.0051893);
        String pickupAddress = "서울 서초구 잠원동 10-1";
        BigDecimal pickupLat = BigDecimal.valueOf(37.5160000);
        BigDecimal pickupLng = BigDecimal.valueOf(127.0200000);
        int hourlyPay = 15_000;
        LocalDateTime recruitStartAt = LocalDateTime.now().plusDays(1);
        LocalDateTime recruitEndAt = LocalDateTime.now().plusDays(6);
        LocalDateTime escortStartAt = LocalDateTime.now().plusDays(7);
        LocalDateTime escortEndAt = LocalDateTime.now().plusDays(7).plusHours(4);
        PostWriteRequest postWriteRequest1 = new PostWriteRequest(
                title, content, postRegion, hospitalName, hospitalAddress, hospitalLat, hospitalLng,
                pickupAddress, pickupLat, pickupLng, hourlyPay, recruitStartAt, recruitEndAt, escortStartAt, escortEndAt,
                RideSelect.TAXI, RideSelect.TAXI, "", true
        );

        PostWriteResponse post1 = postService.write(user1.getId(), postWriteRequest1);

        PostWriteRequest postWriteRequest2 = new PostWriteRequest(
                title + "2", content + "2", postRegion + "2", hospitalName + "2", hospitalAddress + "2",
                hospitalLat, hospitalLng, pickupAddress + "2", pickupLat, pickupLng, hourlyPay, recruitStartAt, recruitEndAt, escortStartAt, escortEndAt,
                RideSelect.TAXI, RideSelect.TAXI, "", true
        );

        PostWriteResponse post2 = postService.write(user1.getId(), postWriteRequest2);

        savedUser1Id = user1.getId();
        savedUser2Id = user2.getId();
        savedRide1Id = rideService.findByPostId(post1.id()).getFirst().getId();
        savedRide2Id = rideService.findByPostId(post2.id()).getFirst().getId();
        savedPost1Id = post1.id();
        savedPost2Id = post2.id();

        // user1로 로그인해 인증 쿠키 확보
        accessTokenCookie1 = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(username, password))
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        accessTokenCookie2 = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(username + "2", password + "2"))
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");
    }

    @Test
    @DisplayName("[RideController] 해당 공고 이동 목록 - 동행 매니저 조회 성공")
    void listByPostEscortSuccess() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/rides/posts/%s".formatted(savedPost1Id))
                                .cookie(accessTokenCookie2))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(RideController.class));
        resultActions.andExpect(handler().methodName("getListByPostId"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.statusCode").value("200-21"));
        resultActions.andExpect(jsonPath("$.msg").value("공고글 이동 정보를 불러왔습니다."));
        resultActions.andExpect(jsonPath("$.data[0].direction").value("TO_HOSPITAL"));
        resultActions.andExpect(jsonPath("$.data[0].selected").value("TAXI"));
    }

    @Test
    @DisplayName("[RideController] 이동 상세 정보 - 성공")
    void rideDetails() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/rides/%s".formatted(savedRide1Id))
                                .cookie(accessTokenCookie1))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(RideController.class));
        resultActions.andExpect(handler().methodName("getRide"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.statusCode").value("200-22"));
        resultActions.andExpect(jsonPath("$.msg").value("이동 정보를 불러왔습니다."));
        resultActions.andExpect(jsonPath("$.data.direction").value("TO_HOSPITAL"));
        resultActions.andExpect(jsonPath("$.data.status").value("ACCEPTED"));
        resultActions.andExpect(jsonPath("$.data.selected").value("TAXI")); // 공고 등록 때 고른 이동수단이 그대로 들어갑니다
    }

    @Test
    @DisplayName("[RideController] 이동 상세 정보 - 이동 정보 없음")
    void rideDetailsFailNotFound() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/rides/%s".formatted(10000))
                                .cookie(accessTokenCookie1))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(RideController.class));
        resultActions.andExpect(handler().methodName("getRide"));
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.statusCode").value("404-20"));
        resultActions.andExpect(jsonPath("$.msg").value("찾으시는 이동 정보가 없습니다."));
    }

    @Test
    @DisplayName("[RideController] 이동 상세 정보 - 권한 부족")
    void rideDetailsFailForbidden() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/rides/%s".formatted(savedRide1Id))
                                .cookie(accessTokenCookie2))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(RideController.class));
        resultActions.andExpect(handler().methodName("getRide"));
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.statusCode").value("403-20"));
        resultActions.andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }
}