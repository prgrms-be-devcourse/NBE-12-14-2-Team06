package com.back.nbe12142team06.domain.education.controller;

import com.back.nbe12142team06.domain.education.entity.EducationProgress;
import com.back.nbe12142team06.domain.education.entity.EducationVideo;
import com.back.nbe12142team06.domain.education.entity.WatchProgressLog;
import com.back.nbe12142team06.domain.education.repository.EducationProgressRepository;
import com.back.nbe12142team06.domain.education.repository.EducationVideoRepository;
import com.back.nbe12142team06.domain.education.repository.WatchProgressLogRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class EducationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EscortProfileRepository escortProfileRepository;

    @Autowired
    private EducationVideoRepository educationVideoRepository;

    @Autowired
    private EducationProgressRepository educationProgressRepository;

    @Autowired
    private WatchProgressLogRepository watchProgressLogRepository;

    // 교육 영상 생성 (60초, 필수)
    private EducationVideo createVideo() {
        return educationVideoRepository.save(
                new EducationVideo("동행 서비스 기본 교육", "/videos/sample_video.mp4", 60, true)
        );
    }

    // 역할 지정 회원가입 후 accessToken 쿠키 반환
    private Cookie signUp(String username, String role) throws Exception {
        String signUpBody = """
                {
                    "username": "%s",
                    "password": "testPassword",
                    "email": "%s@user.user",
                    "name": "김춘식",
                    "role": "%s",
                    "gender": "MALE",
                    "birthDate": "1990-05-20",
                    "phoneNum": "%s010-8080-0000",
                    "region": "서울시"
                }
                """.formatted(username, username, role, username);

        Cookie accessToken = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        assertThat(accessToken).isNotNull();
        return accessToken;
    }

    // 동행 매니저 프로필 생성 (교육 진행 상황도 함께 생성됨)
    private void createEscortProfile(Cookie escortToken) throws Exception {
        mvc.perform(post("/api/v1/users/profile/escort")
                        .cookie(escortToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "intro": "동행 매니저입니다.",
                                    "bankName": "오픈은행",
                                    "accountHolder": "김춘식",
                                    "accountNumber": "123-0000000-123"
                                }
                                """))
                .andExpect(status().isOk());
    }

    // 영상 생성 → 동행 매니저 가입 → 프로필 생성까지 한 번에
    private Cookie escortReady(String username) throws Exception {
        Cookie escortToken = signUp(username, "ESCORT");
        createEscortProfile(escortToken);
        return escortToken;
    }

    private Long findUserId(String username) {
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    // 시청 기록 요청
    private ResultActions watchLog(Long videoId, String body, Cookie token) throws Exception {
        return mvc.perform(post("/api/v1/education-videos/{videoId}/watchlogs", videoId)
                        .cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print());
    }

    private String position(String positionSec) {
        return """
                {
                    "positionSec": %s
                }
                """.formatted(positionSec);
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 정상 기록 시 200-1 반환")
    void t1() throws Exception {
        EducationVideo video = createVideo();
        Cookie escortToken = escortReady("escort1");

        ResultActions resultActions = watchLog(video.getId(), position("0"), escortToken);

        resultActions
                .andExpect(handler().handlerType(EducationController.class))
                .andExpect(handler().methodName("createLog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("시청 기록이 저장되었습니다."))
                .andExpect(jsonPath("$.data.maxWatchedSec").value(0.0))
                .andExpect(jsonPath("$.data.completed").value(false))
                .andExpect(jsonPath("$.data.verified").value(false));

        em.flush();
        em.clear();

        List<WatchProgressLog> logs = watchProgressLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).isAccepted()).isTrue();
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 앞으로 건너뛴 위치는 인정되지 않고 로그만 남음")
    void t2() throws Exception {
        EducationVideo video = createVideo();
        Cookie escortToken = escortReady("escort1");

        watchLog(video.getId(), position("0"), escortToken)
                .andExpect(status().isOk());

        // 직후 30초 위치 보고 → 경과 시간이 거의 없어 허용치 초과
        ResultActions resultActions = watchLog(video.getId(), position("30"), escortToken);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.data.maxWatchedSec").value(0.0))
                .andExpect(jsonPath("$.data.completed").value(false));

        em.flush();
        em.clear();

        List<WatchProgressLog> logs = watchProgressLogRepository.findAll();
        assertThat(logs).hasSize(2);
        assertThat(logs.get(1).isAccepted()).isFalse();
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 필수 영상을 끝까지 보면 완료 및 교육 이수 처리")
    void t3() throws Exception {
        EducationVideo video = createVideo();
        Cookie escortToken = escortReady("escort1");
        Long escortId = findUserId("escort1");

        // 60초 전부터 10초 간격으로 50초까지 정상 시청한 상태를 미리 만듦
        EducationProgress progress = educationProgressRepository
                .findByEscortProfileUserIdAndEducationVideoId(escortId, video.getId())
                .orElseThrow();

        LocalDateTime base = LocalDateTime.now().minusSeconds(60);
        for (int sec = 0; sec <= 50; sec += 10) {
            progress.record(sec, base.plusSeconds(sec));
        }

        // 마지막 기록 약 10초 뒤, 영상 종료 위치 보고
        ResultActions resultActions = watchLog(video.getId(), position("60.1"), escortToken);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.data.maxWatchedSec").value(60.1))
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.verified").value(true));

        em.flush();
        em.clear();

        EscortProfile updated = escortProfileRepository.findById(escortId).orElseThrow();
        assertThat(updated.getVerified()).isTrue();
        assertThat(updated.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 이미 교육을 이수한 동행 매니저는 기록이 쌓이지 않음")
    void t4() throws Exception {
        EducationVideo video = createVideo();
        Cookie escortToken = escortReady("escort1");
        Long escortId = findUserId("escort1");

        escortProfileRepository.findById(escortId).orElseThrow()
                .verify(LocalDateTime.now());

        ResultActions resultActions = watchLog(video.getId(), position("0"), escortToken);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.data.verified").value(true));

        em.flush();
        em.clear();

        assertThat(watchProgressLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 재생 위치 누락 시 400-1 반환")
    void t5() throws Exception {
        EducationVideo video = createVideo();
        Cookie escortToken = escortReady("escort1");

        ResultActions resultActions = watchLog(video.getId(), "{}", escortToken);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("positionSec: 재생 위치는 필수입니다."));
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 음수 재생 위치 요청 시 400-1 반환")
    void t6() throws Exception {
        EducationVideo video = createVideo();
        Cookie escortToken = escortReady("escort1");

        ResultActions resultActions = watchLog(video.getId(), position("-1"), escortToken);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("positionSec: 재생 위치는 0 이상이어야 합니다."));
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 존재하지 않는 영상 요청 시 404 반환")
    void t7() throws Exception {
        createVideo();
        Cookie escortToken = escortReady("escort1");

        ResultActions resultActions = watchLog(99999L, position("0"), escortToken);

        resultActions
                .andExpect(handler().handlerType(EducationController.class))
                .andExpect(handler().methodName("createLog"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지않는 교육 영상입니다."));
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 의뢰인 요청 시 403-1 반환")
    void t8() throws Exception {
        EducationVideo video = createVideo();
        Cookie clientToken = signUp("client1", "CLIENT");

        ResultActions resultActions = watchLog(video.getId(), position("0"), clientToken);

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("[EducationController] 시청 기록 - 로그인 없이 요청 시 401-1 반환")
    void t9() throws Exception {
        EducationVideo video = createVideo();

        ResultActions resultActions = mvc.perform(post("/api/v1/education-videos/{videoId}/watchlogs", video.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(position("0")))
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("[EducationController] 교육 영상 목록 조회 - 정상 조회 시 200-1 반환")
    void t10() throws Exception {
        EducationVideo video = createVideo();
        Cookie escortToken = escortReady("escort1");

        em.flush();
        em.clear();

        ResultActions resultActions = mvc.perform(get("/api/v1/education-videos")
                        .cookie(escortToken))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(EducationController.class))
                .andExpect(handler().methodName("getVideos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("교육 영상 목록을 조회했습니다."))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].videoId").value(video.getId()))
                .andExpect(jsonPath("$.data[0].title").value("동행 서비스 기본 교육"))
                .andExpect(jsonPath("$.data[0].url").value("/videos/sample_video.mp4"))
                .andExpect(jsonPath("$.data[0].durationSec").value(60))
                .andExpect(jsonPath("$.data[0].required").value(true))
                .andExpect(jsonPath("$.data[0].maxWatchedSec").value(0.0))
                .andExpect(jsonPath("$.data[0].completed").value(false));
    }

    @Test
    @DisplayName("[EducationController] 교육 영상 목록 조회 - 프로필 없는 동행 매니저 조회 시 404 반환")
    void t11() throws Exception {
        createVideo();
        Cookie escortToken = signUp("escort1", "ESCORT");   // 프로필 생성 안 함

        ResultActions resultActions = mvc.perform(get("/api/v1/education-videos")
                        .cookie(escortToken))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(EducationController.class))
                .andExpect(handler().methodName("getVideos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg").value("동행인 프로필이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("[EducationController] 교육 영상 단건 조회 - 정상 조회 시 200-1 반환")
    void t12() throws Exception {
        EducationVideo video = createVideo();
        Cookie escortToken = escortReady("escort1");

        em.flush();
        em.clear();

        ResultActions resultActions = mvc.perform(get("/api/v1/education-videos/{videoId}", video.getId())
                        .cookie(escortToken))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(EducationController.class))
                .andExpect(handler().methodName("getVideo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("교육 영상을 조회했습니다."))
                .andExpect(jsonPath("$.data.videoId").value(video.getId()))
                .andExpect(jsonPath("$.data.maxWatchedSec").value(0.0))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    @DisplayName("[EducationController] 교육 영상 단건 조회 - 존재하지 않는 영상 조회 시 404 반환")
    void t13() throws Exception {
        createVideo();
        Cookie escortToken = escortReady("escort1");

        ResultActions resultActions = mvc.perform(get("/api/v1/education-videos/{videoId}", 99999L)
                        .cookie(escortToken))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(EducationController.class))
                .andExpect(handler().methodName("getVideo"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지않는 교육 영상입니다."));
    }
}