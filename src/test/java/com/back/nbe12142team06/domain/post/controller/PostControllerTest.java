package com.back.nbe12142team06.domain.post.controller;

import com.back.nbe12142team06.domain.post.entity.Post;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PostControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PostService postService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testUserId;
    private Cookie accessTokenCookie;

    private Cookie login(String username, String password) throws Exception {
        return mvc.perform(
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
    }

    @BeforeEach
    void setUp() throws Exception {
        String username = "testUser";
        String password = "testPassword";

        User user = new User(
                username,
                passwordEncoder.encode(password),
                "test@test.com",
                "테스트유저",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                "서울"
        );
        testUserId = userRepository.save(user).getId();

        // 로그인해 인증 쿠키 확보
        accessTokenCookie = login(username, password);
    }

    //공고등록
    private Long registerPost() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recruitStartAt = now.plusDays(1);
        LocalDateTime recruitEndAt = now.plusDays(2);
        LocalDateTime escortStartAt = now.plusDays(3);
        LocalDateTime escortEndAt = escortStartAt.plusHours(3);

        String response = mvc
                .perform(post("/api/v1/posts")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt)))
                .andReturn().getResponse().getContentAsString();

        com.jayway.jsonpath.DocumentContext ctx = com.jayway.jsonpath.JsonPath.parse(response);
        return ((Number) ctx.read("$.data.id")).longValue();
    }

    @Test
    @DisplayName("[PostController] 공고 목록 조회 - 정상 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts")
                        .cookie(new Cookie("accessToken", "")))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray());

    }

    @Test
    @DisplayName("[PostController] 공고 목록 조회 - 데이터 없을 때 빈 배열 반환")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts")
                        .cookie(new Cookie("accessToken", "")))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("[PostController] 공고 상세 조회 - 정상 조회")
    void t3() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recruitStartAt = now.plusDays(1);
        LocalDateTime recruitEndAt = now.plusDays(2);
        LocalDateTime escortStartAt = now.plusDays(3);
        LocalDateTime escortEndAt = escortStartAt.plusHours(3);

        // 먼저 공고 등록
        ResultActions writeResult = mvc
                .perform(post("/api/v1/posts")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt)));

        com.jayway.jsonpath.DocumentContext ctx = com.jayway.jsonpath.JsonPath.parse(
                writeResult.andReturn().getResponse().getContentAsString()
        );
        Long postId = ((Number) ctx.read("$.data.id")).longValue();

        // 등록된 공고 조회
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/{id}", postId)
                        .cookie(new Cookie("accessToken", "")))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.postStatus").exists());
    }

    @Test
    @DisplayName("[PostController] 공고 상세 조회 - 존재하지 않는 id 조회 시 404 반환")
    void t4() throws Exception {
        Long notExistingId = 999L;

        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/{id}", notExistingId)
                        .cookie(new Cookie("accessToken", "")))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("detail"))
                .andExpect(status().isNotFound());
    }

    // 시간 순서: 모집시작 -> 모집마감 -> 동행시작 -> 동행종료
    private String validPostJson(LocalDateTime recruitStartAt, LocalDateTime recruitEndAt,
                                 LocalDateTime escortStartAt, LocalDateTime escortEndAt) {
        return """
                {
                    "title": "정형외과 동행 구합니다",
                    "content": "무릎 수술 후 검진 예약이 있어 동행인이 필요합니다.",
                    "region": "서울",
                    "hospitalName": "서울성모병원",
                    "hospitalAddress": "서울 서초구 반포대로 222",
                    "hospitalLat": 37.5012743,
                    "hospitalLng": 127.0051893,
                    "pickupAddress": "서울 서초구 잠원동 10-1",
                    "pickupLat": 37.5160000,
                    "pickupLng": 127.0200000,
                    "hourlyPay": 15000,
                    "recruitStartAt": "%s",
                    "recruitEndAt": "%s",
                    "escortStartAt": "%s",
                    "escortEndAt": "%s",
                    "patientNote": "거동이 불편하신 70대 어르신",
                    "reportRequired": true
                }
                """.formatted(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt);
    }

    @Test
    @DisplayName("[PostController] 공고 등록 - 정상 등록")
    void t5() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recruitStartAt = now.plusDays(1);
        LocalDateTime recruitEndAt = now.plusDays(2);
        LocalDateTime escortStartAt = now.plusDays(3);
        LocalDateTime escortEndAt = escortStartAt.plusHours(3);

        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-1"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.postStatus").value("모집 중"));
    }

    @Test
    @DisplayName("[PostController] 공고 등록 - 필수값 누락 시 400 반환")
    void t6() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recruitStartAt = now.plusDays(1);
        LocalDateTime recruitEndAt = now.plusDays(2);
        LocalDateTime escortStartAt = now.plusDays(3);
        LocalDateTime escortEndAt = escortStartAt.plusHours(3);

        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "content": "",
                                    "region": "",
                                    "hospitalName": "서울성모병원",
                                    "hospitalAddress": "서울 서초구 반포대로 222",
                                    "hospitalLat": 37.5012743,
                                    "hospitalLng": 127.0051893,
                                    "pickupAddress": "서울 서초구 잠원동 10-1",
                                    "pickupLat": 37.5160000,
                                    "pickupLng": 127.0200000,
                                    "hourlyPay": 15000,
                                    "recruitStartAt": "%s",
                                    "recruitEndAt": "%s",
                                    "escortStartAt": "%s",
                                    "escortEndAt": "%s",
                                    "reportRequired": true
                                }
                                """.formatted(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-1"));
    }

    @Test
    @DisplayName("[PostController] 공고 등록 - 시급 0 이하 시 400 반환")
    void t7() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recruitStartAt = now.plusDays(1);
        LocalDateTime recruitEndAt = now.plusDays(2);
        LocalDateTime escortStartAt = now.plusDays(3);
        LocalDateTime escortEndAt = escortStartAt.plusHours(3);

        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "정형외과 동행 구합니다",
                                    "content": "무릎 수술 후 검진 예약이 있어 동행인이 필요합니다.",
                                    "region": "서울",
                                    "hospitalName": "서울성모병원",
                                    "hospitalAddress": "서울 서초구 반포대로 222",
                                    "hospitalLat": 37.5012743,
                                    "hospitalLng": 127.0051893,
                                    "pickupAddress": "서울 서초구 잠원동 10-1",
                                    "pickupLat": 37.5160000,
                                    "pickupLng": 127.0200000,
                                    "hourlyPay": 0,
                                    "recruitStartAt": "%s",
                                    "recruitEndAt": "%s",
                                    "escortStartAt": "%s",
                                    "escortEndAt": "%s",
                                    "reportRequired": true
                                }
                                """.formatted(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-1"));
    }

    @Test
    @DisplayName("[PostController] 공고 등록 - 동행 시작 시간이 종료 시간보다 늦을 때 400 반환")
    void t8() throws Exception {
        // escortStartAt > escortEndAt만 위반하고, 나머지는 현재 시각 기준 상대값으로 설정
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recruitStartAt = now.plusDays(1);
        LocalDateTime recruitEndAt = now.plusDays(2);
        LocalDateTime escortEndAt = now.plusDays(3);
        LocalDateTime escortStartAt = escortEndAt.plusHours(3); // 일부러 종료 시간보다 늦게 설정

        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-2"))
                .andExpect(jsonPath("$.msg").value("동행 시작 시간은 종료 시간보다 빨라야 합니다."));
    }

    @Test
    @DisplayName("[PostController] 공고 등록 - 마감 시간이 동행 시작 시간보다 늦을 때 400 반환")
    void t9() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime escortStartAt = now.plusDays(3);
        LocalDateTime escortEndAt = escortStartAt.plusHours(3);
        LocalDateTime recruitStartAt = now.plusDays(1);
        LocalDateTime recruitEndAt = escortStartAt.plusDays(1); // 일부러 escortStartAt보다 늦게 설정

        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-3"))
                .andExpect(jsonPath("$.msg").value("모집 마감 시간은 동행 시작 시간보다 빨라야 합니다."));
    }

    @Test
    @DisplayName("[PostController] 공고 삭제 - 의뢰인 본인이 작성한 공고가 아닌 경우")
    void t10() throws Exception {
        // testUser로 공고 등록
        Long postId = registerPost();

        // 다른 유저 생성 후 로그인
        String otherUsername = "otherUser";
        String otherPassword = "otherPassword";
        User otherUser = new User(
                otherUsername,
                passwordEncoder.encode(otherPassword),
                "other@test.com",
                "다른유저",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                "010-9999-9999",
                "부산"
        );
        userRepository.save(otherUser);
        Cookie otherAccessTokenCookie = login(otherUsername, otherPassword);

        // 다른 유저로 삭제 시도
        ResultActions resultActions = mvc
                .perform(delete("/api/v1/posts/{id}", postId)
                        .cookie(otherAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-14"))
                .andExpect(jsonPath("$.msg").value("본인이 작성한 공고만 삭제할 수 있습니다."));
    }
    @Test
    @DisplayName("[PostController] 공고 취소 - 관리자는 소유자가 아니어도 매칭된 공고 취소 가능")
    void t11() throws Exception {
        // testUser로 공고 등록
        Long postId = registerPost();

        // TODO : 매칭 로직은 아직 미구현이라 테스트에서 직접 MATCHED로 전환
        Post post = postRepository.findById(postId).orElseThrow();
        post.match();
        postRepository.saveAndFlush(post);

        // 관리자 계정 생성 후 로그인
        String adminUsername = "adminUser";
        String adminPassword = "adminPassword";
        User admin = new User(
                adminUsername,
                passwordEncoder.encode(adminPassword),
                "admin@test.com",
                "관리자",
                Role.ADMIN,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                "010-0000-0000",
                "서울"
        );
        userRepository.save(admin);
        Cookie adminAccessTokenCookie = login(adminUsername, adminPassword);

        // 소유자가 아닌 관리자 계정으로 매칭 취소 시도 -> 성공해야 함
        ResultActions resultActions = mvc
                .perform(patch("/api/v1/posts/{postId}/matchedCancel", postId)
                        .cookie(adminAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("matchedCancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"));
    }
    @Test
    @DisplayName("[PostController] 공고 취소 - 의뢰인 본인이 매칭된 공고 취소 성공")
    void t12() throws Exception {
        Long postId = registerPost();
        Post post = postRepository.findById(postId).orElseThrow();
        post.match();
        postRepository.saveAndFlush(post);

        ResultActions resultActions = mvc
                .perform(patch("/api/v1/posts/{postId}/matchedCancel", postId)
                        .cookie(accessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("matchedCancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"));
    }
    @Test
    @DisplayName("[PostController] 공고 취소 - 매칭 안 된 상태에서 취소 시도 시 400 반환")
    void t13() throws Exception {
        Long postId = registerPost(); // OPEN 상태 그대로

        ResultActions resultActions = mvc
                .perform(patch("/api/v1/posts/{postId}/matchedCancel", postId)
                        .cookie(accessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("matchedCancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-13"));
    }
    @Test
    @DisplayName("[PostController] 공고 취소 - 본인이 작성한 공고가 아닌 경우 401 반환")
    void t14() throws Exception {
        Long postId = registerPost();
        Post post = postRepository.findById(postId).orElseThrow();
        post.match();
        postRepository.saveAndFlush(post);

        String otherUsername = "otherUser2";
        String otherPassword = "otherPassword2";
        User otherUser = new User(
                otherUsername, passwordEncoder.encode(otherPassword),
                "other2@test.com", "다른유저2", Role.CLIENT, Gender.MALE,
                LocalDate.of(1990, 1, 1), "010-1111-2222", "부산"
        );
        userRepository.save(otherUser);
        Cookie otherAccessTokenCookie = login(otherUsername, otherPassword);

        ResultActions resultActions = mvc
                .perform(patch("/api/v1/posts/{postId}/matchedCancel", postId)
                        .cookie(otherAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-16"));
    }
    @Test
    @DisplayName("[PostController] 공고 취소 - 동행인(ESCORT) 역할은 취소 권한 없음 401 반환")
    void t15() throws Exception {
        Long postId = registerPost();
        Post post = postRepository.findById(postId).orElseThrow();
        post.match();
        postRepository.saveAndFlush(post);

        String escortUsername = "escortUser";
        String escortPassword = "escortPassword";
        User escortUser = new User(
                escortUsername, passwordEncoder.encode(escortPassword),
                "escort@test.com", "동행인", Role.ESCORT, Gender.MALE,
                LocalDate.of(1990, 1, 1), "010-3333-4444", "인천"
        );
        userRepository.save(escortUser);
        Cookie escortAccessTokenCookie = login(escortUsername, escortPassword);

        ResultActions resultActions = mvc
                .perform(patch("/api/v1/posts/{postId}/matchedCancel", postId)
                        .cookie(escortAccessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-15"));
    }
    @Test
    @DisplayName("[PostController] 공고 목록 조회 - 결제 취소된 공고는 제외")
    void t16() throws Exception {
        Long postId = registerPost();

        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> p.getPost().getId().equals(postId))
                .findFirst()
                .orElseThrow();
        payment.cancelPayment("테스트 취소");
        paymentRepository.saveAndFlush(payment);

        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts")
                        .cookie(new Cookie("accessToken", "")))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }
    @Test
    @DisplayName("[PostController] 공고 목록 조회 - 취소 후 재결제되면 다시 노출")
    void t17() throws Exception {
        Long postId = registerPost();

        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> p.getPost().getId().equals(postId))
                .findFirst()
                .orElseThrow();
        Payment repayment = payment.cancelPayment("테스트 취소"); // 기존 건 취소 + 재결제용 새 객체 반환
        paymentRepository.saveAndFlush(payment);   // 취소 처리 저장
        paymentRepository.save(repayment);         // 재결제 건 저장

        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts")
                        .cookie(new Cookie("accessToken", "")))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(postId));
    }
    @Test
    @DisplayName("[PostService] 만료 배치 - 모집마감 지난 OPEN 공고는 EXPIRED로 전환된다")
    void t18() {
        User client = userRepository.findById(testUserId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        // API로는 과거 날짜 공고를 못 만드니 리포지토리로 직접 생성
        Post expiredTargetPost = Post.builder()
                .client(client)
                .title("만료 테스트 공고")
                .content("만료 배치 테스트용")
                .region("서울")
                .hospitalName("테스트병원")
                .hospitalAddress("테스트주소")
                .hospitalLat(BigDecimal.valueOf(37.5))
                .hospitalLng(BigDecimal.valueOf(127.0))
                .pickupAddress("테스트픽업주소")
                .pickupLat(BigDecimal.valueOf(37.5))
                .pickupLng(BigDecimal.valueOf(127.0))
                .hourlyPay(15000)
                .recruitStartAt(now.minusDays(3))
                .recruitEndAt(now.minusDays(1))     // 이미 지난 마감시간
                .escortStartAt(now.plusDays(1))
                .escortEndAt(now.plusDays(1).plusHours(3))
                .build();
        Long targetPostId = postRepository.save(expiredTargetPost).getId();

        // when
        postService.expireOverduePosts();

        // then
        Post result = postRepository.findById(targetPostId).orElseThrow();
        assertThat(result.getPostStatus()).isEqualTo(PostStatus.EXPIRED);
    }
    @Test
    @DisplayName("[PostService] 만료 배치 - 매칭된 공고는 마감시간 지나도 대상에서 제외된다")
    void t19() {
        User client = userRepository.findById(testUserId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        //registerPost() 대신 builder()를 써야 시간 체크 피해감
        Post matchedPost = Post.builder()
                .client(client)
                .title("매칭된 공고")
                .content("만료 배치 제외 테스트용")
                .region("서울")
                .hospitalName("테스트병원")
                .hospitalAddress("테스트주소")
                .hospitalLat(BigDecimal.valueOf(37.5))
                .hospitalLng(BigDecimal.valueOf(127.0))
                .pickupAddress("테스트픽업주소")
                .pickupLat(BigDecimal.valueOf(37.5))
                .pickupLng(BigDecimal.valueOf(127.0))
                .hourlyPay(15000)
                .recruitStartAt(now.minusDays(3))
                .recruitEndAt(now.minusDays(1))     // 마감 지남
                .escortStartAt(now.plusDays(1))
                .escortEndAt(now.plusDays(1).plusHours(3))
                .postStatus(PostStatus.MATCHED)     // 이미 매칭된 상태로 직접 세팅
                .build();
        Long matchedPostId = postRepository.save(matchedPost).getId();

        // when
        postService.expireOverduePosts();

        // then
        Post result = postRepository.findById(matchedPostId).orElseThrow();
        assertThat(result.getPostStatus()).isEqualTo(PostStatus.MATCHED); // 그대로 유지돼야 함
    }
}
