package com.back.nbe12142team06.domain.post.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("[PostController] 공고 목록 조회 - 정상 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray());
              //  .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("[PostController] 공고 목록 조회 - 데이터 없을 때 빈 배열 반환")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("[PostController] 공고 상세 조회 - 정상 조회")
    void t3() throws Exception {
        Long existingId = 1L;

        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/{id}", existingId))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(existingId))
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.postStatus").exists());
    }

    @Test
    @DisplayName("[PostController] 공고 상세 조회 - 존재하지 않는 id 조회 시 404 반환")
    void t4() throws Exception {
        Long notExistingId = 999L;

        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/{id}", notExistingId))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("detail"))
                .andExpect(status().isNotFound());
    }

    private String validPostJson(String escortStartAt, String escortEndAt, String deadlineAt) {
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
                    "escortStartAt": "%s",
                    "escortEndAt": "%s",
                    "deadlineAt": "%s",
                    "patientNote": "거동이 불편하신 70대 어르신",
                    "reportRequired": true
                }
                """.formatted(escortStartAt, escortEndAt, deadlineAt);
    }

    @Test
    @DisplayName("[PostController] 공고 등록 - 정상 등록")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson(
                                "2026-09-17T10:00:00",
                                "2026-09-17T13:00:00",
                                "2026-09-15T10:00:00"
                        )))
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
        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "content": "",
                                    "region": ""
                                }
                                """))
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
        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
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
                                    "escortStartAt": "2026-09-17T10:00:00",
                                    "escortEndAt": "2026-09-17T13:00:00",
                                    "deadlineAt": "2026-09-16T10:00:00",
                                    "reportRequired": true
                                }
                                """))
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
        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson(
                                "2026-09-17T13:00:00",  // 시작이 더 늦음
                                "2026-09-17T10:00:00",  // 종료가 더 빠름
                                "2026-09-16T10:00:00"
                        )))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg").value("동행 시작 시간은 종료 시간보다 빨라야 합니다."));
    }

    @Test
    @DisplayName("[PostController] 공고 등록 - 마감 시간이 동행 시작 시간보다 늦을 때 400 반환")
    void t9() throws Exception {
        ResultActions resultActions = mvc
                .perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson(
                                "2026-09-17T10:00:00",
                                "2026-09-17T13:00:00",
                                "2026-09-18T10:00:00"   // 마감이 시작보다 늦음
                        )))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.msg").value("모집 마감 시간은 동행 시작 시간보다 빨라야 합니다."));
    }
}
