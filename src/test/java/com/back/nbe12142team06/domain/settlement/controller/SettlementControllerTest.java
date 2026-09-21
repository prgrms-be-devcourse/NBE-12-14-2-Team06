package com.back.nbe12142team06.domain.settlement.controller;

import com.back.nbe12142team06.domain.application.dto.ApplicationAcceptResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationApplyResponse;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.domain.payment.client.TossPaymentClient;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
    private EscortProfileRepository escortProfileRepository;

    @MockitoBean
    private TossPaymentClient tossPaymentClient;

    private User savedUser1;
    private User savedUser2;
    private User savedUser3;
    private Settlement savedSettlement1;
    private Long savedSettlement1Id;
    private Post savedPost1;
    private Cookie accessTokenCookie2;
    private Cookie accessTokenCookie3;
    @Autowired
    private ApplicationRepository applicationRepository;

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
                username + "2", password + "2", email + "2", name + "2", Role.valueOf(roleEscort),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                "010-9999-9991", region));

        savedUser3 = userService.signUp(new UserSignUpRequest(
                username+"3", password+"3", email+"3", name+"3", Role.valueOf(roleEscort),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                "010-9999-9992", region));

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

        postService.write(savedUser1.getId(), postWriteRequest2);

        ApplicationApplyResponse applyResponse1 = applicationService.apply(savedPost1.getId(), savedUser2.getId());
        ApplicationAcceptResponse acceptResponse1 = applicationService.accept(applyResponse1.id(), savedUser1.getId());

        Application application1 = applicationRepository.findAllByPostIdWithEscort(savedPost1.getId()).stream().findFirst().orElse(null);

        savedSettlement1 = settlementService.createSettlement(savedPost1.getTotalPay().intValue(), application1, savedUser2, LocalDate.now().plusDays(1));
        savedSettlement1Id = savedSettlement1.getId();

        EscortProfile escortProfile1 = new EscortProfile(savedUser2).updateAccount("오픈은행", savedUser2.getName(), "000-1234567-000");
        escortProfileRepository.save(escortProfile1);
        EscortProfile escortProfile2 = new EscortProfile(savedUser3).updateAccount("오픈은행", savedUser3.getName(), "111-7654321-111");
        escortProfileRepository.save(escortProfile2);

        // user로 로그인해 인증 쿠키 확보
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
    }

    private void complete() {

    }

    @Test
    @DisplayName("[SettlementController] 정산 요청 - 성공")
    void settlementReq() throws Exception {

        savedPost1.complete(LocalDateTime.now()); //post.complete() 변경으로 now() 추가

        ResultActions resultActions = mvc.perform(
                        post("/api/v1/settlements/%s".formatted(savedSettlement1Id))
                                .cookie(accessTokenCookie2))
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
                                .cookie(accessTokenCookie2))
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
                                .cookie(accessTokenCookie3))
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
                        post("/api/v1/settlements/%s".formatted(savedSettlement1Id))
                                .cookie(accessTokenCookie2))
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
                                .cookie(accessTokenCookie2))
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
                                .cookie(accessTokenCookie2))
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
                                .cookie(accessTokenCookie2))
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
                                .cookie(accessTokenCookie3))
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
                .application(null)
                .escort(savedUser2)
                .build();

        Settlement save = settlementRepository.save(settlement);

        int[] counts = settlementService.settlementProcess();

        Settlement savedSettlement = settlementRepository.findById(save.getId()).orElse(null);

        assertEquals(SettlementStatus.COMPLETED, savedSettlement.getSettlementStatus());
        assertEquals(1, counts[0]);
        assertEquals(1, counts[1]);
        assertEquals(0, counts[2]);
    }
}