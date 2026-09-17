package com.back.nbe12142team06.domain.review.controller;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.review.entity.Review;
import com.back.nbe12142team06.domain.review.entity.ReviewTag;
import com.back.nbe12142team06.domain.review.repository.ReviewRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ReviewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long acceptedApplicationId;   // 매칭 확정된 동행 건
    private Long pendingApplicationId;    // 아직 확정되지 않은 동행 건

    private Cookie clientCookie;   // 해당 공고를 등록한 의뢰인
    private Cookie otherCookie;    // 동행 건과 무관한 제3자

    @BeforeEach
    void setUp() throws Exception {

        User client = new User(
                "client1", passwordEncoder.encode("testPassword"), "client1@test.com",
                "의뢰인1", Role.CLIENT, Gender.MALE,
                LocalDate.of(1950, 1, 1), "010-1111-1111", "수원"
        );
        userRepository.save(client);

        User escort = new User(
                "escort1", passwordEncoder.encode("testPassword"), "escort1@test.com",
                "동행인1", Role.ESCORT, Gender.FEMALE,
                LocalDate.of(1995, 1, 1), "010-2222-2222", "수원"
        );
        userRepository.save(escort);

        User other = new User(
                "other1", passwordEncoder.encode("testPassword"), "other1@test.com",
                "제3자1", Role.CLIENT, Gender.MALE,
                LocalDate.of(1990, 1, 1), "010-3333-3333", "수원"
        );
        userRepository.save(other);

        Post post = createPost(client);
        postRepository.save(post);

        // 매칭 확정된 동행 건
        Application accepted = Application.builder()
                .post(post)
                .escort(escort)
                .status(ApplicationStatus.ACCEPTED)
                .build();
        applicationRepository.save(accepted);
        acceptedApplicationId = accepted.getId();

        // 아직 확정되지 않은 동행 건
        Application pending = Application.builder()
                .post(post)
                .escort(other)
                .status(ApplicationStatus.PENDING)
                .build();
        applicationRepository.save(pending);
        pendingApplicationId = pending.getId();

        clientCookie = login("client1");
        otherCookie = login("other1");
    }

    private Post createPost(User client) {
        return Post.builder()
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
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now().plusDays(1))
                .escortStartAt(LocalDateTime.now().plusDays(2))
                .escortEndAt(LocalDateTime.now().plusDays(2).plusHours(3))
                .build();
    }

    // 로그인 후 accessToken 쿠키 획득
    private Cookie login(String username) throws Exception {
        return mvc.perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "%s",
                                          "password": "testPassword"
                                        }
                                        """.formatted(username))
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");
    }

    @Test
    @DisplayName("[ReviewController] 리뷰 작성 - 정상 등록")
    void 리뷰_작성_성공() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/reviews".formatted(acceptedApplicationId))
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5,
                                    "tags": ["KIND", "PUNCTUAL"],
                                    "content": "어머니를 세심하게 챙겨주셨습니다."
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-1"))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.tags", hasSize(2)))
                .andExpect(jsonPath("$.data.applicationId").value(acceptedApplicationId));
    }

    @Test
    @DisplayName("[ReviewController] 리뷰 작성 - 태그와 내용 없이 별점만으로 등록")
    void 리뷰_작성_별점만() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/reviews".formatted(acceptedApplicationId))
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 4
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rating").value(4))
                .andExpect(jsonPath("$.data.tags", hasSize(0)));
    }

    @Test
    @DisplayName("[ReviewController] 리뷰 작성 - 존재하지 않는 동행 건일 때 404-1 반환")
    void 리뷰_작성_동행건_없음() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/999999/reviews")
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404-1"));
    }

    @Test
    @DisplayName("[ReviewController] 리뷰 작성 - 본인이 의뢰한 동행 건이 아닐 때 403-1 반환")
    void 리뷰_작성_권한_없음() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/reviews".formatted(acceptedApplicationId))
                        .cookie(otherCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 1
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403-1"));
    }

    @Test
    @DisplayName("[ReviewController] 리뷰 작성 - 매칭 확정되지 않은 동행 건일 때 400-1 반환")
    void 리뷰_작성_미확정() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/reviews".formatted(pendingApplicationId))
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-1"));
    }

    @Test
    @DisplayName("[ReviewController] 리뷰 작성 - 이미 리뷰가 존재할 때 409-1 반환")
    void 리뷰_작성_중복() throws Exception {

        reviewRepository.save(
                Review.builder()
                        .application(applicationRepository.findById(acceptedApplicationId).orElseThrow())
                        .rating(5)
                        .tags(Set.of(ReviewTag.KIND))
                        .content("기존 리뷰")
                        .build()
        );

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/reviews".formatted(acceptedApplicationId))
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 3
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-1"));
    }

    @Test
    @DisplayName("[ReviewController] 리뷰 작성 - 별점이 범위를 벗어날 때 400 반환")
    void 리뷰_작성_별점_범위_초과() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/reviews".formatted(acceptedApplicationId))
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 6
                                }
                                """)
        ).andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[ReviewController] 리뷰 작성 - 태그를 6개 이상 선택할 때 400 반환")
    void 리뷰_작성_태그_초과() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/reviews".formatted(acceptedApplicationId))
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5,
                                    "tags": ["KIND", "PUNCTUAL", "DETAILED_REPORT",
                                             "GOOD_COMMUNICATION", "CAREFUL", "LATE"]
                                }
                                """)
        ).andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }
}