package com.back.nbe12142team06.domain.report.controller;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.report.repository.ReportRepository;
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
import org.springframework.http.MediaType;
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
public class ReportControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testApplicationId;

    @BeforeEach
    void setUp() {

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
                .build();
        postRepository.save(post);

        Application application = Application.builder()
                .post(post)
                .escort(escort)
                .build();
        applicationRepository.save(application);

        testApplicationId = application.getId();
    }

    @Test
    @DisplayName("[ReportController] 진료 보고서 작성 - 정상 등록")
    void 보고서_작성_성공() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "정형외과 진료 결과",
                                    "originContent": "무릎 통증으로 내원하셨고 물리치료 처방을 받으셨습니다."
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-1"))
                .andExpect(jsonPath("$.data.title").value("정형외과 진료 결과"));
    }

    @Test
    @DisplayName("[ReportController] 진료 보고서 작성 - 존재하지 않는 동행 건일 때 404 반환")
    void 보고서_작성_동행건_없음() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/999999/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "제목",
                                    "originContent": "내용"
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404-1"));
    }

    @Test
    @DisplayName("[ReportController] 진료 보고서 작성 - 이미 보고서가 존재할 때 409 반환")
    void 보고서_작성_중복() throws Exception {

        reportRepository.save(
                Report.builder()
                        .application(applicationRepository.findById(testApplicationId).orElseThrow())
                        .title("기존 보고서")
                        .originContent("기존 내용")
                        .build()
        );

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "새 보고서",
                                    "originContent": "새 내용"
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("409-1"));
    }

    @Test
    @DisplayName("[ReportController] 진료 보고서 작성 - 필수값 누락 시 400 반환")
    void 보고서_작성_필수값_누락() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "originContent": ""
                                }
                                """)
        ).andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }
}