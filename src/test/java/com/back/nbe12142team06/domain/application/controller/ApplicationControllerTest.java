package com.back.nbe12142team06.domain.application.controller;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EscortRepository escortRepository;

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

        // 테스트용 동행인 프로필 생성
        EscortProfile escortProfile = new EscortProfile(escort);
        escortRepository.save(escortProfile);

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

        // 테스트 과정에 결제 데이터가 필요해서 추가
        Payment payment = Payment.builder()
                .amount(post.getTotalPay().intValue())
                .hourlyPaySnapshot(post.getHourlyPay())
                .hours(post.getEscortHours())
                .post(post)
                .build();

        paymentRepository.save(payment);

        // 의뢰인 로그인
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

        // 동행인 로그인
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
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].applicationId").exists())
                .andExpect(jsonPath("$.data.content[0].escortId").exists())
                .andExpect(jsonPath("$.data.content[0].escortName").value("동행인1"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));
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
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0));
    }

    @Test
    @DisplayName("[ApplicationController] 공고별 지원 목록 조회 - 페이징 정상 동작")
    void t7_1() throws Exception {

        Post post = postRepository.findById(testPostId)
                .orElseThrow();

        User escort1 = userRepository.findByUsername("escort1")
                .orElseThrow();

        User escort2 = new User(
                "escort2",
                passwordEncoder.encode("testPassword"),
                "escort2@test.com",
                "동행인2",
                Role.ESCORT,
                Gender.MALE,
                LocalDate.of(1995, 2, 2),
                "010-2222-3333",
                "서울"
        );

        User escort3 = new User(
                "escort3",
                passwordEncoder.encode("testPassword"),
                "escort3@test.com",
                "동행인3",
                Role.ESCORT,
                Gender.FEMALE,
                LocalDate.of(1995, 3, 3),
                "010-2222-4444",
                "인천"
        );

        userRepository.save(escort2);
        userRepository.save(escort3);

        applicationRepository.save(
                Application.builder()
                        .post(post)
                        .escort(escort1)
                        .build()
        );

        applicationRepository.save(
                Application.builder()
                        .post(post)
                        .escort(escort2)
                        .build()
        );

        applicationRepository.save(
                Application.builder()
                        .post(post)
                        .escort(escort3)
                        .build()
        );

        ResultActions resultActions = mvc.perform(
                get("/api/v1/applications/posts/{postId}", testPostId)
                        .param("page", "0")
                        .param("size", "2")
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.content[0].escortName").value("동행인3"))
                .andExpect(jsonPath("$.data.content[1].escortName").value("동행인2"));
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
                .andExpect(jsonPath("$.statusCode").value("403"))
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
                patch("/api/v1/applications/{applicationId}/accept", application.getId())
                        .cookie(otherClientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403"))
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
    @DisplayName("[ApplicationController] 지원 승인 - 한 명 승인 시 나머지 지원자는 자동 거절")
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
                "인천"
        );

        userRepository.save(escort2);

        Cookie escort2AccessTokenCookie = mvc.perform(
                        post("/api/v1/auth/login")
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
                        patch("/api/v1/applications/{applicationId}/accept",
                                firstApplication.getId())
                                .cookie(clientAccessTokenCookie)
                )
                .andDo(print())
                .andExpect(status().isOk());

        // 승인된 지원 상태 확인
        Application acceptedApplication = applicationRepository
                .findById(firstApplication.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.ACCEPTED,
                acceptedApplication.getStatus()
        );

        // 나머지 지원자는 자동 거절됐는지 확인
        Application rejectedApplication = applicationRepository
                .findById(secondApplication.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.REJECTED,
                rejectedApplication.getStatus()
        );

        // 공고도 매칭 완료됐는지 확인
        Post matchedPost = postRepository
                .findById(testPostId)
                .orElseThrow();

        assertEquals(
                PostStatus.MATCHED,
                matchedPost.getPostStatus()
        );

        // 이미 자동 거절된 두 번째 지원을 다시 승인 시도
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/accept",
                        secondApplication.getId())
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg")
                        .value("대기 중인 지원만 승인할 수 있습니다."));
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

    @Test
    @DisplayName("[ApplicationController] 지원 승인 - 같은 동행인의 시간 겹치는 다른 지원 자동 거절")
    void t15() throws Exception {

        User client = userRepository.findByUsername("client1")
                .orElseThrow();

        // 기존 testPost와 시간이 겹치는 두 번째 공고 생성
        Post overlappingPost = Post.builder()
                .client(client)
                .title("시간 겹치는 테스트 공고")
                .content("동행 시간이 겹치는 공고입니다.")
                .region("수원")
                .hospitalName("성빈센트병원")
                .hospitalAddress("경기도 수원시")
                .hospitalLat(BigDecimal.valueOf(37.2770))
                .hospitalLng(BigDecimal.valueOf(127.0276))
                .pickupAddress("경기도 수원시")
                .pickupLat(BigDecimal.valueOf(37.2700))
                .pickupLng(BigDecimal.valueOf(127.0300))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now().plusDays(1))
                .recruitEndAt(LocalDateTime.now().plusDays(2))

                // 기존 testPost: 3일 뒤 ~ 3일 뒤 + 3시간
                // 새 공고: 3일 뒤 + 1시간 ~ 3일 뒤 + 4시간
                // -> 서로 시간이 겹침
                .escortStartAt(LocalDateTime.now().plusDays(3).plusHours(1))
                .escortEndAt(LocalDateTime.now().plusDays(3).plusHours(4))

                .patientNote("테스트 환자")
                .reportRequired(false)
                .build();

        Long overlappingPostId = postRepository.save(overlappingPost).getId();

        // 같은 동행인이 첫 번째 공고에 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        // 같은 동행인이 시간이 겹치는 두 번째 공고에도 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", overlappingPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        // 첫 번째 공고 지원 조회
        Application firstApplication = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 두 번째 공고 지원 조회
        Application overlappingApplication = applicationRepository
                .findAllByPostIdWithEscort(overlappingPostId)
                .get(0);

        // 첫 번째 공고에서 동행인 선정
        mvc.perform(
                        patch("/api/v1/applications/{applicationId}/accept",
                                firstApplication.getId())
                                .cookie(clientAccessTokenCookie)
                )
                .andDo(print())
                .andExpect(status().isOk());

        // DB 상태 다시 조회
        Application acceptedApplication = applicationRepository
                .findById(firstApplication.getId())
                .orElseThrow();

        Application rejectedApplication = applicationRepository
                .findById(overlappingApplication.getId())
                .orElseThrow();

        // 선택된 지원은 ACCEPTED
        assertEquals(
                ApplicationStatus.ACCEPTED,
                acceptedApplication.getStatus()
        );

        // 같은 동행인의 시간이 겹치는 다른 지원은 자동 REJECTED
        assertEquals(
                ApplicationStatus.REJECTED,
                rejectedApplication.getStatus()
        );

        // 선정된 공고는 MATCHED
        Post matchedPost = postRepository
                .findById(testPostId)
                .orElseThrow();

        assertEquals(
                PostStatus.MATCHED,
                matchedPost.getPostStatus()
        );

        // 다른 공고 자체는 계속 모집 가능
        Post otherPost = postRepository
                .findById(overlappingPostId)
                .orElseThrow();

        assertEquals(
                PostStatus.OPEN,
                otherPost.getPostStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 승인 - 같은 동행인의 시간이 겹치지 않는 다른 지원은 대기 유지")
    void t16() throws Exception {

        User client = userRepository.findByUsername("client1")
                .orElseThrow();

        // 기존 testPost와 시간이 겹치지 않는 두 번째 공고 생성
        Post nonOverlappingPost = Post.builder()
                .client(client)
                .title("시간 안 겹치는 테스트 공고")
                .content("동행 시간이 겹치지 않는 공고입니다.")
                .region("수원")
                .hospitalName("성빈센트병원")
                .hospitalAddress("경기도 수원시")
                .hospitalLat(BigDecimal.valueOf(37.2770))
                .hospitalLng(BigDecimal.valueOf(127.0276))
                .pickupAddress("경기도 수원시")
                .pickupLat(BigDecimal.valueOf(37.2700))
                .pickupLng(BigDecimal.valueOf(127.0300))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now().plusDays(1))
                .recruitEndAt(LocalDateTime.now().plusDays(2))

                // 기존 testPost: 3일 뒤 ~ 3일 뒤 + 3시간
                // 새 공고:3일 뒤 + 4시간 ~ 3일 뒤 + 6시간
                // -> 시간이 겹치지 않음
                .escortStartAt(LocalDateTime.now().plusDays(3).plusHours(4))
                .escortEndAt(LocalDateTime.now().plusDays(3).plusHours(6))

                .patientNote("테스트 환자")
                .reportRequired(false)
                .build();

        Long nonOverlappingPostId =
                postRepository.save(nonOverlappingPost).getId();

        // 같은 동행인이 첫 번째 공고에 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        // 같은 동행인이 시간이 안 겹치는 두 번째 공고에도 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", nonOverlappingPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        // 첫 번째 공고 지원 조회
        Application firstApplication = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 두 번째 공고 지원 조회
        Application secondApplication = applicationRepository
                .findAllByPostIdWithEscort(nonOverlappingPostId)
                .get(0);

        // 첫 번째 공고에서 동행인 선정
        mvc.perform(
                        patch("/api/v1/applications/{applicationId}/accept",
                                firstApplication.getId())
                                .cookie(clientAccessTokenCookie)
                )
                .andDo(print())
                .andExpect(status().isOk());

        // DB 상태 다시 조회
        Application acceptedApplication = applicationRepository
                .findById(firstApplication.getId())
                .orElseThrow();

        Application pendingApplication = applicationRepository
                .findById(secondApplication.getId())
                .orElseThrow();

        // 첫 번째 지원은 ACCEPTED
        assertEquals(
                ApplicationStatus.ACCEPTED,
                acceptedApplication.getStatus()
        );

        // 시간이 겹치지 않는 다른 지원은 PENDING 유지
        assertEquals(
                ApplicationStatus.PENDING,
                pendingApplication.getStatus()
        );

        // 첫 번째 공고는 MATCHED
        Post matchedPost = postRepository
                .findById(testPostId)
                .orElseThrow();

        assertEquals(
                PostStatus.MATCHED,
                matchedPost.getPostStatus()
        );

        // 두 번째 공고는 계속 OPEN
        Post otherPost = postRepository
                .findById(nonOverlappingPostId)
                .orElseThrow();

        assertEquals(
                PostStatus.OPEN,
                otherPost.getPostStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 승인 - 이미 승인된 다른 공고와 시간이 겹치면 승인 불가")
    void t17() throws Exception {

        // 첫 번째 공고에 동행인이 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        Application firstApplication = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 첫 번째 공고에서 동행인 승인
        mvc.perform(
                        patch("/api/v1/applications/{applicationId}/accept",
                                firstApplication.getId())
                                .cookie(clientAccessTokenCookie)
                )
                .andExpect(status().isOk());

        // 첫 번째 지원이 실제 ACCEPTED인지 확인
        Application acceptedApplication = applicationRepository
                .findById(firstApplication.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.ACCEPTED,
                acceptedApplication.getStatus()
        );

        // 두 번째 의뢰인 생성
        User client2 = new User(
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

        userRepository.save(client2);

        // 두 번째 의뢰인 로그인
        Cookie client2AccessTokenCookie = mvc.perform(
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

        // 첫 번째 공고와 시간이 겹치는 두 번째 공고 생성
        Post overlappingPost = Post.builder()
                .client(client2)
                .title("시간 겹치는 두 번째 공고")
                .content("이미 승인된 일정과 겹치는 공고입니다.")
                .region("수원")
                .hospitalName("성빈센트병원")
                .hospitalAddress("경기도 수원시")
                .hospitalLat(BigDecimal.valueOf(37.2770))
                .hospitalLng(BigDecimal.valueOf(127.0276))
                .pickupAddress("경기도 수원시")
                .pickupLat(BigDecimal.valueOf(37.2700))
                .pickupLng(BigDecimal.valueOf(127.0300))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now().plusDays(1))
                .recruitEndAt(LocalDateTime.now().plusDays(2))

                // 기존 testPost: 3일 뒤 ~ 3일 뒤 + 3시간
                // 새 공고: 3일 뒤 + 1시간 ~ 3일 뒤 + 4시간
                // -> 시간 겹침
                .escortStartAt(LocalDateTime.now().plusDays(3).plusHours(1))
                .escortEndAt(LocalDateTime.now().plusDays(3).plusHours(4))

                .patientNote("테스트 환자")
                .reportRequired(false)
                .build();

        Long overlappingPostId =
                postRepository.save(overlappingPost).getId();

        // 이미 다른 공고에서 ACCEPTED된 동행인이
        // 시간이 겹치는 새 공고에 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", overlappingPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        Application overlappingApplication = applicationRepository
                .findAllByPostIdWithEscort(overlappingPostId)
                .get(0);

        // 두 번째 의뢰인이 해당 동행인을 선정하려고 시도
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/accept",
                        overlappingApplication.getId())
                        .cookie(client2AccessTokenCookie)
        ).andDo(print());

        // 이미 승인된 일정과 시간이 겹치므로 승인 실패
        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("accept"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg")
                        .value("이미 매칭된 다른 공고와 동행 시간이 겹칩니다."));

        // 승인 실패했으므로 두 번째 지원은 PENDING 유지
        Application pendingApplication = applicationRepository
                .findById(overlappingApplication.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.PENDING,
                pendingApplication.getStatus()
        );

        // 두 번째 공고 역시 OPEN 유지
        Post stillOpenPost = postRepository
                .findById(overlappingPostId)
                .orElseThrow();

        assertEquals(
                PostStatus.OPEN,
                stillOpenPost.getPostStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 거절 - 정상 거절")
    void t18() throws Exception {

        // 동행인이 먼저 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .cookie(escortAccessTokenCookie))
                .andExpect(status().isCreated());

        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 의뢰인이 지원 거절
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/reject", application.getId())
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("지원 거절이 완료되었습니다."));

        Application rejectedApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.REJECTED,
                rejectedApplication.getStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 거절 - 다른 의뢰인이 거절 시 403 반환")
    void t19() throws Exception {

        // 동행인이 먼저 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
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

        // 다른 의뢰인 로그인
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

        // 다른 의뢰인이 지원 거절 시도
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/reject",
                        application.getId())
                        .cookie(otherClientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("reject"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403"))
                .andExpect(jsonPath("$.msg")
                        .value("본인 공고의 지원만 거절할 수 있습니다."));

        // 실패했으므로 상태는 여전히 PENDING
        Application pendingApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.PENDING,
                pendingApplication.getStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 거절 - 이미 거절된 지원 재거절 시 400 반환")
    void t20() throws Exception {

        // 동행인이 먼저 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 첫 번째 거절
        mvc.perform(
                        patch("/api/v1/applications/{applicationId}/reject",
                                application.getId())
                                .cookie(clientAccessTokenCookie)
                )
                .andExpect(status().isOk());

        // 같은 지원을 다시 거절
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/reject",
                        application.getId())
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("reject"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg")
                        .value("대기 중인 지원만 거절할 수 있습니다."));

        // 상태는 그대로 REJECTED
        Application rejectedApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.REJECTED,
                rejectedApplication.getStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 거절 - 존재하지 않는 지원 거절 시 404 반환")
    void t21() throws Exception {

        Long notExistingApplicationId = 999L;

        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/reject",
                        notExistingApplicationId)
                        .cookie(clientAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("reject"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg")
                        .value("지원을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("[ApplicationController] 지원 취소 - 정상 취소")
    void t22() throws Exception {

        // 동행인이 먼저 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 동행인이 본인의 지원 취소
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/cancel",
                        application.getId())
                        .cookie(escortAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg")
                        .value("지원 취소가 완료되었습니다."));

        // 실제 DB 상태 확인
        Application canceledApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.CANCELED,
                canceledApplication.getStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 취소 - 승인 후 모집 마감 전 취소")
    void t23() throws Exception {

        // 동행인이 공고에 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 의뢰인이 지원 승인
        mvc.perform(
                        patch("/api/v1/applications/{applicationId}/accept",
                                application.getId())
                                .cookie(clientAccessTokenCookie)
                )
                .andExpect(status().isOk());

        // 취소 전 노쇼 횟수 확인
        EscortProfile escortProfile = escortRepository
                .findById(application.getEscort().getId())
                .orElseThrow();

        int beforeNoShowCount = escortProfile.getNoShowCount();

        // 승인된 동행인이 취소
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/cancel",
                        application.getId())
                        .cookie(escortAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg")
                        .value("지원 취소가 완료되었습니다."));

        // 취소 후 지원 상태 확인
        Application canceledApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        // 취소 후 공고 상태 확인
        Post reopenedPost = postRepository
                .findById(testPostId)
                .orElseThrow();

        // 취소 후 동행인 프로필 확인
        EscortProfile updatedEscortProfile = escortRepository
                .findById(application.getEscort().getId())
                .orElseThrow();

        // 승인된 지원은 NO_SHOW 처리
        assertEquals(
                ApplicationStatus.NO_SHOW,
                canceledApplication.getStatus()
        );

        // 재매칭을 위해 acceptedPostId 초기화
        assertNull(canceledApplication.getAcceptedPostId());

        // 동행인 노쇼 횟수 1 증가
        assertEquals(
                beforeNoShowCount + 1,
                updatedEscortProfile.getNoShowCount()
        );

        // 모집 마감 전이므로 공고 다시 OPEN
        assertEquals(
                PostStatus.OPEN,
                reopenedPost.getPostStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 취소 - 승인 후 모집 마감 후 취소")
    void t24() throws Exception {

        User client = userRepository.findByUsername("client1")
                .orElseThrow();

        User escort = userRepository.findByUsername("escort1")
                .orElseThrow();

        // 모집 마감 시간이 이미 지난 매칭 공고 생성
        Post expiredMatchedPost = Post.builder()
                .client(client)
                .title("모집 마감 지난 공고")
                .content("모집 마감 후 동행인 취소 테스트")
                .region("수원")
                .hospitalName("아주대학교병원")
                .hospitalAddress("경기도 수원시")
                .hospitalLat(BigDecimal.valueOf(37.2795))
                .hospitalLng(BigDecimal.valueOf(127.0476))
                .pickupAddress("경기도 수원시 팔달구")
                .pickupLat(BigDecimal.valueOf(37.2636))
                .pickupLng(BigDecimal.valueOf(127.0286))
                .hourlyPay(15000)

                // 모집 마감 시간이 이미 지남
                .recruitStartAt(LocalDateTime.now().minusDays(2))
                .recruitEndAt(LocalDateTime.now().minusMinutes(1))

                // 실제 동행 시간은 아직 미래
                .escortStartAt(LocalDateTime.now().plusDays(1))
                .escortEndAt(LocalDateTime.now().plusDays(1).plusHours(3))

                .patientNote("테스트 환자")
                .reportRequired(false)
                .postStatus(PostStatus.MATCHED)
                .build();

        postRepository.save(expiredMatchedPost);

        // 이미 승인된 지원 생성
        Application application = Application.builder()
                .post(expiredMatchedPost)
                .escort(escort)
                .build();

        application.accept();
        applicationRepository.save(application);

        // 취소 전 노쇼 횟수
        EscortProfile escortProfile = escortRepository
                .findById(escort.getId())
                .orElseThrow();

        int beforeNoShowCount = escortProfile.getNoShowCount();

        // 승인된 동행인이 취소
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/cancel",
                        application.getId())
                        .cookie(escortAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg")
                        .value("지원 취소가 완료되었습니다."));

        // 취소 후 지원 확인
        Application canceledApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        // 취소 후 공고 확인
        Post canceledPost = postRepository
                .findById(expiredMatchedPost.getId())
                .orElseThrow();

        // 취소 후 동행인 프로필 확인
        EscortProfile updatedEscortProfile = escortRepository
                .findById(escort.getId())
                .orElseThrow();

        // 승인된 지원은 NO_SHOW
        assertEquals(
                ApplicationStatus.NO_SHOW,
                canceledApplication.getStatus()
        );

        // 재매칭할 수 없으므로 기존 승인 공고 ID 초기화
        assertNull(canceledApplication.getAcceptedPostId());

        // 노쇼 횟수 +1
        assertEquals(
                beforeNoShowCount + 1,
                updatedEscortProfile.getNoShowCount()
        );

        // 모집 마감 후이므로 공고 취소
        assertEquals(
                PostStatus.CANCELED,
                canceledPost.getPostStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 취소 - 다른 동행인이 취소 시 403 반환")
    void t25() throws Exception {

        // 기존 동행인(escort1)이 공고에 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 다른 동행인 생성
        User otherEscort = new User(
                "escort2",
                passwordEncoder.encode("testPassword"),
                "escort2@test.com",
                "동행인2",
                Role.ESCORT,
                Gender.MALE,
                LocalDate.of(1996, 1, 1),
                "010-3333-3333",
                "서울"
        );

        userRepository.save(otherEscort);

        // 다른 동행인 로그인
        Cookie otherEscortAccessTokenCookie = mvc.perform(
                        post("/api/v1/auth/login")
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

        // 다른 동행인이 escort1의 지원을 취소 시도
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/cancel",
                        application.getId())
                        .cookie(otherEscortAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403"))
                .andExpect(jsonPath("$.msg")
                        .value("본인이 지원한 내역만 취소할 수 있습니다."));

        // 취소되지 않고 기존 PENDING 상태 유지
        Application unchangedApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.PENDING,
                unchangedApplication.getStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 취소 - 이미 취소된 지원 재취소 시 400 반환")
    void t26() throws Exception {

        // 동행인이 공고에 지원
        mvc.perform(
                        post("/api/v1/applications/{postId}", testPostId)
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isCreated());

        Application application = applicationRepository
                .findAllByPostIdWithEscort(testPostId)
                .get(0);

        // 첫 번째 취소
        mvc.perform(
                        patch("/api/v1/applications/{applicationId}/cancel",
                                application.getId())
                                .cookie(escortAccessTokenCookie)
                )
                .andExpect(status().isOk());

        // 같은 지원을 다시 취소
        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/cancel",
                        application.getId())
                        .cookie(escortAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg")
                        .value("취소할 수 없는 지원 상태입니다."));

        // 상태는 그대로 CANCELED
        Application canceledApplication = applicationRepository
                .findById(application.getId())
                .orElseThrow();

        assertEquals(
                ApplicationStatus.CANCELED,
                canceledApplication.getStatus()
        );
    }

    @Test
    @DisplayName("[ApplicationController] 지원 취소 - 존재하지 않는 지원 취소 시 404 반환")
    void t27() throws Exception {

        Long notExistingApplicationId = 999L;

        ResultActions resultActions = mvc.perform(
                patch("/api/v1/applications/{applicationId}/cancel",
                        notExistingApplicationId)
                        .cookie(escortAccessTokenCookie)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg")
                        .value("지원을 찾을 수 없습니다."));
    }
}
