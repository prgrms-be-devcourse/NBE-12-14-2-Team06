package com.back.nbe12142team06.domain.application.controller;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long testPostId;
    private Cookie clientAccessTokenCookie;
    private Cookie escortAccessTokenCookie;

    @BeforeEach
    void setUp() throws Exception {

        // 공고를 작성할 의뢰인
        User client = new User(
                "client1",
                passwordEncoder.encode("testPassword"),
                "client1@test.com",
                "의뢰인1",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                "010-1111-1111",
                "수원"
        );

        userRepository.save(client);

        // 공고에 지원할 동행인
        User escort = new User(
                "escort1",
                passwordEncoder.encode("testPassword"),
                "escort1@test.com",
                "동행인1",
                Role.ESCORT,
                Gender.FEMALE,
                LocalDate.of(1995, 1, 1),
                "010-2222-2222",
                "수원"
        );

        userRepository.save(escort);

        // 테스트용 공고
        Post post = Post.builder()
                .client(client)
                .title("테스트 공고")
                .content("병원 동행 테스트")
                .region("수원")
                .hospitalName("아주대학교병원")
                .hospitalAddress("경기도 수원시")
                .hospitalLat(BigDecimal.valueOf(37.2795))
                .hospitalLng(BigDecimal.valueOf(127.0476))
                .pickupAddress("경기도 수원시 팔달구")
                .pickupLat(BigDecimal.valueOf(37.2636))
                .pickupLng(BigDecimal.valueOf(127.0286))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now().plusDays(1))
                .recruitEndAt(LocalDateTime.now().plusDays(2))
                .escortStartAt(LocalDateTime.now().plusDays(3))
                .escortEndAt(LocalDateTime.now().plusDays(3).plusHours(3))
                .patientNote("테스트 환자")
                .reportRequired(false)
                .build();

        testPostId = postRepository.save(post).getId();

        clientAccessTokenCookie = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                  "username": "client1",
                                  "password": "testPassword"
                                }
                                """)
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        escortAccessTokenCookie = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                  "username": "escort1",
                                  "password": "testPassword"
                                }
                                """)
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");
    }

    @Test
    @DisplayName("[ApplicationController] 지원하기 - 정상 지원")
    void t1() throws Exception {

        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("apply"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("지원이 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.postId").value(testPostId))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("[ApplicationController] 지원하기 - 중복 지원 시 409 반환")
    void t2() throws Exception {

        // 첫 번째 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print())
                .andExpect(status().isCreated());

        // 같은 사용자가 같은 공고에 두 번째 지원
        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("apply"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409"))
                .andExpect(jsonPath("$.msg").value("이미 지원한 공고입니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 지원하기 - CLIENT가 지원 시 400 반환")
    void t3() throws Exception {

        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(clientAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("apply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg")
                        .value("동행인만 공고에 지원할 수 있습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 지원하기 - 모집 중이 아닌 공고 지원 시 400 반환")
    void t4() throws Exception {

        User client = userRepository.findByUsername("client1")
                .orElseThrow();

        // 이미 매칭 완료된 공고 생성
        Post matchedPost = Post.builder()
                .client(client)
                .title("매칭 완료된 공고")
                .content("이미 매칭된 공고입니다.")
                .region("수원")
                .hospitalName("아주대학교병원")
                .hospitalAddress("경기도 수원시")
                .hospitalLat(BigDecimal.valueOf(37.2795))
                .hospitalLng(BigDecimal.valueOf(127.0476))
                .pickupAddress("경기도 수원시 팔달구")
                .pickupLat(BigDecimal.valueOf(37.2636))
                .pickupLng(BigDecimal.valueOf(127.0286))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now().plusDays(1))
                .recruitEndAt(LocalDateTime.now().plusDays(2))
                .escortStartAt(LocalDateTime.now().plusDays(3))
                .escortEndAt(LocalDateTime.now().plusDays(3).plusHours(3))
                .patientNote("테스트 환자")
                .reportRequired(false)
                .postStatus(PostStatus.MATCHED)
                .build();

        Long matchedPostId = postRepository.save(matchedPost).getId();

        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", matchedPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("apply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg")
                        .value("모집 중인 공고에만 지원할 수 있습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 지원하기 - 존재하지 않는 공고 지원 시 404 반환")
    void t5() throws Exception {

        Long notExistingPostId = 999L;

        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", notExistingPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("apply"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg")
                        .value("공고를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 공고별 지원 목록 조회 - 정상 조회")
    void t6() throws Exception {

        // 먼저 해당 공고에 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print())
                .andExpect(status().isCreated());

        // 지원 목록 조회
        ResultActions resultActions = mvc
                .perform(get("/api/v1/applications/posts/{postId}", testPostId)
                        .cookie(clientAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("지원 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].applicationId").exists())
                .andExpect(jsonPath("$.data[0].escortId").exists())
                .andExpect(jsonPath("$.data[0].escortName").value("동행인1"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("[ApplicationController] 공고별 지원 목록 조회 - 지원자가 없을 때 빈 배열 반환")
    void t7() throws Exception {

        ResultActions resultActions = mvc
                .perform(get("/api/v1/applications/posts/{postId}", testPostId)
                        .cookie(clientAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("지원 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[ApplicationController] 공고별 지원 목록 조회 - 존재하지 않는 공고 조회 시 404 반환")
    void t8() throws Exception {

        Long notExistingPostId = 999L;

        ResultActions resultActions = mvc
                .perform(get("/api/v1/applications/posts/{postId}", notExistingPostId)
                        .cookie(clientAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("list"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg").value("공고를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 공고별 지원 목록 조회 - 다른 의뢰인의 공고 조회 시 403 반환")
    void t9() throws Exception {
        // 다른 의뢰인 생성
        User otherClient = new User(
                "client2",
                passwordEncoder.encode("testPassword"),
                "client2@test.com",
                "의뢰인2",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                "010-3333-3333",
                "서울"
        );

        userRepository.save(otherClient);

        Cookie otherClientAccessTokenCookie = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content("""
                                    {
                                      "username": "client2",
                                      "password": "testPassword"
                                    }
                                    """)
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        ResultActions resultActions = mvc.perform(
                get("/api/v1/applications/posts/{postId}", testPostId)
                        .cookie(otherClientAccessTokenCookie)
        );

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403-1"))
                .andExpect(jsonPath("$.msg")
                        .value("본인 공고의 지원 목록만 조회할 수 있습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 지원 승인 - 정상 승인")
    void t10() throws Exception {

        // 동행인이 먼저 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print())
                .andExpect(status().isCreated());

        // 방금 생성된 지원 조회
        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 의뢰인이 지원 승인
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/accept", application.getId())
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("지원 승인이 완료되었습니다."))
                .andExpect(jsonPath("$.data.applicationId").value(application.getId()))
                .andExpect(jsonPath("$.data.postId").value(testPostId))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        // 승인 후 실제 DB 상태 확인
        Application acceptedApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        Post matchedPost = postRepository
                .findById(testPostId)
                .orElseThrow();

        assertEquals(
                ApplicationStatus.ACCEPTED,
                acceptedApplication.getStatus()
        );

        assertEquals(
                testPostId,
                acceptedApplication.getAcceptedPostId()
        );

        assertEquals(
                PostStatus.MATCHED,
                matchedPost.getPostStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 승인 - 다른 의뢰인이 승인 시 403 반환")
    void t11() throws Exception {

        // 동행인이 먼저 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print())
                .andExpect(status().isCreated());

        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 다른 의뢰인 생성
        User otherClient = new User(
                "client2",
                passwordEncoder.encode("testPassword"),
                "client2@test.com",
                "의뢰인2",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                "010-3333-3333",
                "서울"
        );

        userRepository.save(otherClient);

        Cookie otherClientAccessTokenCookie = mvc.perform(
                        post("/api/v1/users/login")
                                .contentType("application/json")
                                .content("""
                            {
                              "username": "client2",
                              "password": "testPassword"
                            }
                            """)
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/accept", application.getId())
                        .cookie(otherClientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403-1"))
                .andExpect(jsonPath("$.msg")
                        .value("본인 공고의 지원만 승인할 수 있습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 지원 승인 - 이미 승인된 지원 재승인 시 400 반환")
    void t12() throws Exception {

        // 동행인이 먼저 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print())
                .andExpect(status().isCreated());

        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 첫 번째 승인
        mvc.perform(
                        patch("/api/v1/applications/{applicationId}/accept", application.getId())
                                .cookie(clientAccessTokenCookie)
                )
                .andDo(print())
                .andExpect(status().isOk());

        // 같은 지원을 다시 승인
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/accept", application.getId())
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg")
                        .value("대기 중인 지원만 승인할 수 있습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 지원 승인 - 이미 다른 지원자가 매칭된 공고 승인 시 400 반환")
    void t13() throws Exception {

        // 두 번째 동행인 생성
        User escort2 = new User(
                "escort2",
                passwordEncoder.encode("testPassword"),
                "escort2@test.com",
                "동행인2",
                Role.ESCORT,
                Gender.MALE,
                LocalDate.of(1996, 1, 1),
                "010-4444-4444",
                "수원"
        );

        userRepository.save(escort2);

        Cookie escort2AccessTokenCookie = mvc.perform(
                        post("/api/v1/users/login")
                                .contentType("application/json")
                                .content("""
                            {
                              "username": "escort2",
                              "password": "testPassword"
                            }
                            """)
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        // 첫 번째 동행인 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print())
                .andExpect(status().isCreated());

        // 두 번째 동행인 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escort2AccessTokenCookie))
                .andDo(print())
                .andExpect(status().isCreated());

        // 두 지원 조회
        List<Application> applications =
                applicationRepository.findAllByPostIdWithEscort(testPostId);

        Application firstApplication = applications.stream()
                .filter(application ->
                        application.getEscort().getUsername().equals("escort1"))
                .findFirst()
                .orElseThrow();

        Application secondApplication = applications.stream()
                .filter(application ->
                        application.getEscort().getUsername().equals("escort2"))
                .findFirst()
                .orElseThrow();

        // 첫 번째 동행인 승인
        mvc.perform(
                        patch("/api/v1/applications/{applicationId}/accept", firstApplication.getId())
                                .cookie(clientAccessTokenCookie)
                )
                .andDo(print())
                .andExpect(status().isOk());

        // 두 번째 동행인도 승인 시도
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/accept", secondApplication.getId())
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg")
                        .value("모집 중인 공고만 매칭할 수 있습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 지원 승인 - 존재하지 않는 지원 승인 시 404 반환")
    void t14() throws Exception {

        Long notExistingApplicationId = 999L;

        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/accept", notExistingApplicationId)
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("accept"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg")
                        .value("지원을 찾을 수 없습니다."));
    }
}
