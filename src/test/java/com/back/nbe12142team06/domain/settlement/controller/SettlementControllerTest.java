package com.back.nbe12142team06.domain.settlement.controller;

import com.back.nbe12142team06.domain.application.dto.ApplicationAcceptResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationApplyResponse;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.settlement.client.SettlementClient;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientResponse;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortRepository;
import com.back.nbe12142team06.domain.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SettlementControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private SettlementService settlementService;
    @Autowired
    private EscortRepository escortRepository;

    private User savedUser1;
    private User savedUser2;
    private Long savedUser3Id;
    private User savedUser4;
    private Long savedPayment1Id;
    private Long savedPayment2Id;
    private Settlement savedSettlement1;
    private Long savedSettlement1Id;
    private Long savedSettlement2Id;
    private Post savedPost1;
    private Post savedPost2;
    private Long savedApplication1Id;
    private Long savedApplication2Id;
    private Cookie accessTokenCookie1;
    private Cookie accessTokenCookie2;
    private Cookie accessTokenCookie3;
    private Cookie accessTokenCookie4;

    @BeforeEach
    public void init() throws Exception {
        String username = "testUsername";
        String password = "testPassword";
        String email = "testEmail@test.test";
        String name = "김춘식";
        String roleClient = "CLIENT";
        String roleEscort = "ESCORT";
        String gender = "MALE";
        String birthDate = "1990-05-20";
        String phoneNum = "010-1234-5678";
        String region = "서울시";

        savedUser1 = userService.signUp(new UserSignUpRequest(
                username, password, email, name, Role.valueOf(roleClient),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                phoneNum, region));

        savedUser2 = userService.signUp(new UserSignUpRequest(
                username + "2", password + "2", email + "2", name + "2", Role.valueOf(roleClient),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                "010-9999-9991", region));

        User user3 = userService.signUp(new UserSignUpRequest(
                username+"3", password+"3", email+"3", name+"3", Role.valueOf(roleEscort),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                "010-9999-9992", region));
        savedUser3Id = user3.getId();

        User user4 = userService.signUp(new UserSignUpRequest(
                username+"4", password+"4", email+"4", name+"4", Role.valueOf(roleEscort),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                "010-9999-9993", region));
        savedUser4 = user4;

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
                "", true
        );

        savedPost1 = postService.write(savedUser1.getId(), postWriteRequest1);

        PostWriteRequest postWriteRequest2 = new PostWriteRequest(
                title + "2", content + "2", postRegion + "2", hospitalName + "2", hospitalAddress + "2",
                hospitalLat, hospitalLng, pickupAddress + "2", pickupLat, pickupLng, hourlyPay, recruitStartAt, recruitEndAt, escortStartAt, escortEndAt,
                "", true
        );

        savedPost2 = postService.write(savedUser1.getId(), postWriteRequest2);

        ApplicationApplyResponse applyResponse1 = applicationService.apply(savedPost1.getId(), savedUser3Id);
        ApplicationAcceptResponse acceptResponse1 = applicationService.accept(applyResponse1.id(), savedUser1.getId());
        savedApplication1Id = acceptResponse1.applicationId();

        savedSettlement1 = settlementRepository.findByApplicationId(savedApplication1Id).get();
        savedSettlement1Id = savedSettlement1.getId();

        ApplicationApplyResponse applyResponse2 = applicationService.apply(savedPost2.getId(), savedUser4.getId());
        ApplicationAcceptResponse acceptResponse2 = applicationService.accept(applyResponse2.id(), savedUser1.getId());
        savedApplication2Id = acceptResponse2.applicationId();

        savedSettlement2Id = settlementRepository.findByApplicationId(savedApplication2Id).get().getId();

        EscortProfile escortProfile1 = new EscortProfile(user3).updateAccount("오픈은행", user3.getName(), "000-1234567-000");
        escortRepository.save(escortProfile1);
        EscortProfile escortProfile2 = new EscortProfile(user4).updateAccount("오픈은행", user4.getName(), "111-7654321-111");
        escortRepository.save(escortProfile2);

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

        accessTokenCookie3 = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(username + "3", password + "3"))
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        accessTokenCookie4 = mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(username + "4", password + "4"))
                )
                .andReturn()
                .getResponse()
                .getCookie("accessToken");
    }

    @Test
    @DisplayName("[SettlementController] 정산 요청 - 성공")
    void settlementReq() throws Exception {

        savedPost1.complete();

        ResultActions resultActions = mvc.perform(
                        post("/api/v1/settlements/%s".formatted(savedSettlement1Id))
                                .cookie(accessTokenCookie3))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(SettlementController.class));
        resultActions.andExpect(handler().methodName("settlementRequest"));
        resultActions.andExpect(jsonPath("$.statusCode").value("200-30"));
        resultActions.andExpect(jsonPath("$.msg").value("정산에 성공했습니다."));

        Settlement settlement = settlementRepository.findById(savedSettlement1Id).get();
        Assertions.assertEquals(SettlementStatus.COMPLETED, settlement.getSettlementStatus());
    }

    @Test
    @DisplayName("[SettlementController] 정산 요청 - 실패 찾을 수 없음")
    void settlementReqFailNotFound() throws Exception {
        Long settlementId = 10000L;
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/settlements/%s".formatted(settlementId))
                                .cookie(accessTokenCookie3))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(SettlementController.class));
        resultActions.andExpect(handler().methodName("settlementRequest"));
        resultActions.andExpect(jsonPath("$.statusCode").value("404-30"));
        resultActions.andExpect(jsonPath("$.msg").value("찾으시는 정산 데이터가 없습니다."));
    }

    @Test
    @DisplayName("[SettlementController] 정산 요청 - 실패 권한 부족")
    void settlementReqFailForbidden() throws Exception {
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/settlements/%s".formatted(savedSettlement1Id))
                                .cookie(accessTokenCookie4))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(SettlementController.class));
        resultActions.andExpect(handler().methodName("settlementRequest"));
        resultActions.andExpect(jsonPath("$.statusCode").value("403-30"));
        resultActions.andExpect(jsonPath("$.msg").value("정산 요청할 권한이 없습니다."));
    }

    @Test
    @DisplayName("[SettlementController] 정산 요청 - 실패 완료되지 않은 동행")
    void settlementReqFailInvalid() throws Exception {
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/settlements/%s".formatted(savedSettlement2Id))
                                .cookie(accessTokenCookie4))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(SettlementController.class));
        resultActions.andExpect(handler().methodName("settlementRequest"));
        resultActions.andExpect(jsonPath("$.statusCode").value("400-30"));
        resultActions.andExpect(jsonPath("$.msg").value("아직 완료되지 않은 동행 의뢰입니다."));
    }

    @Test
    @DisplayName("[SettlementController] 정산 목록 조회 - 성공")
    void settlementList() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/settlements")
                                .cookie(accessTokenCookie3))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(SettlementController.class));
        resultActions.andExpect(handler().methodName("settlementList"));
        resultActions.andExpect(jsonPath("$.statusCode").value("200-31"));
        resultActions.andExpect(jsonPath("$.msg").value("정산 목록을 가져왔습니다."));
        resultActions.andExpect(jsonPath("$.data[0]").doesNotExist());
    }

    @Test
    @DisplayName("[SettlementController] 정산 상세 조회 - 성공")
    void settlementDetail() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/settlements/%s".formatted(savedSettlement1Id))
                                .cookie(accessTokenCookie3))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(SettlementController.class));
        resultActions.andExpect(handler().methodName("settlementDetail"));
        resultActions.andExpect(jsonPath("$.statusCode").value("200-32"));
        resultActions.andExpect(jsonPath("$.msg").value("정산 상세 데이터를 조회했습니다."));
        resultActions.andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("[SettlementController] 정산 상세 조회 - 실패 찾을 수 없음")
    void settlementDetailFailNotFound() throws Exception {
        Long settlementId = 10000L;
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/settlements/%s".formatted(settlementId))
                                .cookie(accessTokenCookie3))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(SettlementController.class));
        resultActions.andExpect(handler().methodName("settlementDetail"));
        resultActions.andExpect(jsonPath("$.statusCode").value("404-30"));
        resultActions.andExpect(jsonPath("$.msg").value("찾으시는 정산 데이터가 없습니다."));
    }

    @Test
    @DisplayName("[SettlementController] 정산 상세 조회 - 실패 권한 부족")
    void settlementDetailFailForbidden() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/settlements/%s".formatted(savedSettlement1Id))
                                .cookie(accessTokenCookie4))
                .andDo(print());

        resultActions.andExpect(handler().handlerType(SettlementController.class));
        resultActions.andExpect(handler().methodName("settlementDetail"));
        resultActions.andExpect(jsonPath("$.statusCode").value("403-30"));
        resultActions.andExpect(jsonPath("$.msg").value("정산 요청할 권한이 없습니다."));
    }

    @Test
    @DisplayName("[SettlementService] 자동 정산 - 성공")
    void settlementScheduler() {

        Settlement settlement = Settlement.builder()
                .payoutAmount(1000)
                .settlementStatus(SettlementStatus.PENDING)
                .settledDate(LocalDate.now())
                .payment(null)
                .application(null)
                .escort(savedUser4)
                .build();

        Settlement save = settlementRepository.save(settlement);

        int[] counts = settlementService.settlementProcess();

        assertEquals(SettlementStatus.COMPLETED, save.getSettlementStatus());
        assertEquals(1, counts[0]);
        assertEquals(1, counts[1]);
        assertEquals(0, counts[2]);
    }
}