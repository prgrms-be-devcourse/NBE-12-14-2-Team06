package com.back.nbe12142team06.domain.report.controller;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.report.repository.ReportRepository;
import com.back.nbe12142team06.domain.report.service.ReportMasker;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
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

    @Autowired
    private ReportMasker reportMasker;

    private Long testApplicationId;

    private Cookie clientCookie;   // 해당 동행 건의 의뢰인
    private Cookie escortCookie;   // 해당 동행 건의 동행인
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
                "제3자1", Role.ESCORT, Gender.MALE,
                LocalDate.of(1990, 1, 1), "010-3333-3333", "수원"
        );
        userRepository.save(other);

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
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now().plusDays(1))
                .escortStartAt(LocalDateTime.now().plusDays(2))
                .escortEndAt(LocalDateTime.now().plusDays(2).plusHours(3))
                .build();
        postRepository.save(post);

        Application application = Application.builder()
                .post(post)
                .escort(escort)
                .build();
        applicationRepository.save(application);

        testApplicationId = application.getId();

        clientCookie = login("client1");
        escortCookie = login("escort1");
        otherCookie = login("other1");
    }

    // 로그인 후 accessToken 쿠키 획득
    private Cookie login(String username) throws Exception {
        return mvc.perform(
                        post("/api/v1/auth/login")
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

    // ── 보고서 작성 ────────────────────────────

    @Test
    @DisplayName("[ReportController] 진료 보고서 작성 - 정상 등록")
    void 보고서_작성_성공() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .cookie(escortCookie)
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
    @DisplayName("[ReportController] 진료 보고서 작성 - 존재하지 않는 동행 건일 때 404-1 반환")
    void 보고서_작성_동행건_없음() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/999999/report")
                        .cookie(escortCookie)
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
    @DisplayName("[ReportController] 진료 보고서 작성 - 해당 동행인이 아닐 때 403-1 반환")
    void 보고서_작성_권한_없음() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .cookie(otherCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "제목",
                                    "originContent": "내용"
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403-1"));
    }

    @Test
    @DisplayName("[ReportController] 진료 보고서 작성 - 이미 보고서가 존재할 때 409-1 반환")
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
                        .cookie(escortCookie)
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
                        .cookie(escortCookie)
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

    // ── 보고서 조회 ────────────────────────────

    @Test
    @DisplayName("[ReportController] 진료 보고서 조회 - 의뢰인 정상 조회")
    void 보고서_조회_성공() throws Exception {

        reportRepository.save(
                Report.builder()
                        .application(applicationRepository.findById(testApplicationId).orElseThrow())
                        .title("정형외과 진료 결과")
                        .originContent("무릎 통증으로 내원하셨고 물리치료 처방을 받으셨습니다.")
                        .build()
        );

        ResultActions resultActions = mvc.perform(
                get("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .cookie(clientCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.data.title").value("정형외과 진료 결과"))
                .andExpect(jsonPath("$.data.applicationId").value(testApplicationId));
    }

    @Test
    @DisplayName("[ReportController] 진료 보고서 조회 - 제3자가 조회 시 403-2 반환")
    void 보고서_조회_권한_없음() throws Exception {

        reportRepository.save(
                Report.builder()
                        .application(applicationRepository.findById(testApplicationId).orElseThrow())
                        .title("정형외과 진료 결과")
                        .originContent("무릎 통증으로 내원하셨습니다.")
                        .build()
        );

        ResultActions resultActions = mvc.perform(
                get("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .cookie(otherCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403-2"));
    }

    @Test
    @DisplayName("[ReportController] 진료 보고서 조회 - 존재하지 않는 동행 건일 때 404-1 반환")
    void 보고서_조회_동행건_없음() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/applications/999999/report")
                        .cookie(clientCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404-1"));
    }

    @Test
    @DisplayName("[ReportController] 진료 보고서 조회 - 보고서가 아직 작성되지 않았을 때 404-2 반환")
    void 보고서_조회_미작성() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .cookie(clientCookie)
        ).andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404-2"));
    }

    // ── AI 요약 ────────────────────────────────

    @Test
    @DisplayName("[ReportController] 진료 보고서 작성 - 요약이 함께 저장된다")
    void 보고서_작성_시_요약_저장() throws Exception {

        ResultActions resultActions = mvc.perform(
                post("/api/v1/applications/%d/report".formatted(testApplicationId))
                        .cookie(escortCookie)
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
                .andExpect(jsonPath("$.data.aiSummary").isNotEmpty())
                .andExpect(jsonPath("$.data.summarizedAt").isNotEmpty());
    }

    @Test
    @DisplayName("[ReportMasker] 외부 API 전송 전 실명과 연락처가 마스킹된다")
    void 마스킹_적용() {

        Application application = applicationRepository.findById(testApplicationId).orElseThrow();

        String origin = "의뢰인1 어르신께서 무릎 통증을 호소하셨습니다. "
                + "보호자 연락처는 010-9999-8888 입니다. 동행인1 이 함께 이동했습니다.";

        String masked = reportMasker.mask(
                origin,
                application.getPost().getClient(),
                application.getEscort()
        );

        assertThat(masked).doesNotContain("의뢰인1");
        assertThat(masked).doesNotContain("동행인1");
        assertThat(masked).doesNotContain("010-9999-8888");
        assertThat(masked).contains("환자분");
        assertThat(masked).contains("무릎 통증");   // 진료 내용은 유지
    }
}