package com.back.nbe12142team06.domain.application.controller;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
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

    private Long testPostId;

    @BeforeEach
    void setUp() {

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
                .escortStartAt(LocalDateTime.of(2026, 9, 17, 10, 0))
                .escortEndAt(LocalDateTime.of(2026, 9, 17, 13, 0))
                .deadlineAt(LocalDateTime.of(2026, 9, 16, 10, 0))
                .patientNote("테스트 환자")
                .reportRequired(false)
                .build();

        testPostId = postRepository.save(post).getId();
    }

    @Test
    @DisplayName("정상 지원")
    void t1() throws Exception {

        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", testPostId)
                        .param("username", "escort1"))
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
    @DisplayName("중복 지원할 경우")
    void t2() throws Exception {

        // 첫 번째 지원
        mvc.perform(post("/api/v1/applications/{postId}", testPostId)
                        .param("username", "escort1"))
                .andDo(print())
                .andExpect(status().isCreated());

        // 같은 사용자가 같은 공고에 두 번째 지원
        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", testPostId)
                        .param("username", "escort1"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("apply"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409"))
                .andExpect(jsonPath("$.msg").value("이미 지원한 공고입니다."));
    }

    @Test
    @DisplayName("CLIENT가 지원할 경우")
    void t3() throws Exception {

        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", testPostId)
                        .param("username", "client1"))
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
    @DisplayName("OPEN이 아닌 공고에 지원할 경우")
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
                .escortStartAt(LocalDateTime.of(2026, 9, 17, 10, 0))
                .escortEndAt(LocalDateTime.of(2026, 9, 17, 13, 0))
                .deadlineAt(LocalDateTime.of(2026, 9, 16, 10, 0))
                .patientNote("테스트 환자")
                .reportRequired(false)
                .postStatus(PostStatus.MATCHED)
                .build();

        Long matchedPostId = postRepository.save(matchedPost).getId();

        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", matchedPostId)
                        .param("username", "escort1"))
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
    @DisplayName("존재하지 않는 공고에 지원할 경우")
    void t5() throws Exception {

        Long notExistingPostId = 999L;

        ResultActions resultActions = mvc
                .perform(post("/api/v1/applications/{postId}", notExistingPostId)
                        .param("username", "escort1"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApplicationController.class))
                .andExpect(handler().methodName("apply"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg")
                        .value("공고를 찾을 수 없습니다."));
    }
}
