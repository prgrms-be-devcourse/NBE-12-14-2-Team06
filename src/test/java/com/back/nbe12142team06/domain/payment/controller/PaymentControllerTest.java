package com.back.nbe12142team06.domain.payment.controller;

import com.back.nbe12142team06.domain.payment.client.TossPaymentClient;
import com.back.nbe12142team06.domain.payment.dto.PaymentCancelRequest;
import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.payment.service.PaymentPersistenceService;
import com.back.nbe12142team06.domain.payment.service.PaymentService;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.service.UserService;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import jakarta.servlet.http.Cookie;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PaymentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentPersistenceService paymentPersistenceService;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;

    private Long savedUser1Id;
    private Long savedUser2Id;
    private Long savedPayment1Id;
    private Long savedPayment2Id;
    private Cookie accessTokenCookie1;
    private Cookie accessTokenCookie2;


    @BeforeEach
    public void init() throws Exception {
        String username = "testUsername";
        String password = "testPassword";
        String email = "testEmail@test.test";
        String name = "김춘식";
        String role = "CLIENT";
        String gender = "MALE";
        String birthDate = "1990-05-20";
        String phoneNum = "010-1234-5678";
        String region = "서울시";

        User user1 = userService.signUp(new UserSignUpRequest(
                username, password, email, name, Role.valueOf(role),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                phoneNum, region));
        savedUser1Id = user1.getId();

        User user2 = userService.signUp(new UserSignUpRequest(
                username + "2", password + "2", email + "2", name + "2", Role.valueOf(role),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                "010-9999-9999", region));
        savedUser2Id = user2.getId();

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

        Post post1 = postService.write(user1.getId(), postWriteRequest1);

        PostWriteRequest postWriteRequest2 = new PostWriteRequest(
                title + "2", content + "2", postRegion + "2", hospitalName + "2", hospitalAddress + "2",
                hospitalLat, hospitalLng, pickupAddress + "2", pickupLat, pickupLng, hourlyPay, recruitStartAt, recruitEndAt, escortStartAt, escortEndAt,
                "", true
        );

        Post post2 = postService.write(user1.getId(), postWriteRequest2);

        savedPayment1Id = paymentRepository.findByPostIdAndUserId(post1.getId(), savedUser1Id).get().getId();
        savedPayment2Id = paymentRepository.findByPostIdAndUserId(post2.getId(), savedUser1Id).get().getId();

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
    }

    @Test
    @DisplayName("[PaymentController] 결제 승인 - 성공")
    void requestConfirm() {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";

        TossPaymentClient mockTossPaymentClient = mock(TossPaymentClient.class);
        when(mockTossPaymentClient.callApiConfirm(any(), any(), any())).thenReturn(ResponseEntity.ok(new TossConfirmResponse("계좌이체", amount)));

        PaymentService paymentService = new PaymentService(paymentRepository, paymentPersistenceService, mockTossPaymentClient);

        Payment payment = paymentService.confirm(new PaymentConfirmRequest(paymentKey, orderId, amount), savedPayment1Id, savedUser1Id, amount);

        assertEquals(PaymentStatus.DONE, payment.getPaymentStatus());
        assertEquals(LocalDateTime.now().getHour(), payment.getApprovedAt().getHour());
        assertEquals(LocalDateTime.now().getMinute(), payment.getApprovedAt().getMinute());
    }

    /*
    @Test
    @DisplayName("[PaymentController] 결제 승인 - DB 정지")
    void requestConfirmFailDbStop() throws Exception {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";

        ResultActions resultActions = mvc.perform(
                post("/api/v1/payments/%s/confirm".formatted(savedPayment1Id))
                        .cookie(accessTokenCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "paymentKey": "%s",
                                    "orderId": "%s",
                                    "amount": "%s"
                                }
                                """.formatted(paymentKey, orderId, amount))
        ).andDo(print());

        resultActions
                .andExpect(status().isInternalServerError());
    }
     */

    @Test
    @DisplayName("[PaymentController] 결제 승인 - 잘못 요청된 유저")
    void requestConfirmFailUserId() {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";

        PaymentService paymentService = new PaymentService(paymentRepository, null, null);

        // 예외 발생 400번
        assertThrows(InvalidException.class, () -> {
            paymentService.confirm(new PaymentConfirmRequest(paymentKey, orderId, amount), savedPayment1Id, savedUser2Id, amount);
        });
    }

    @Test
    @DisplayName("[PaymentController] 결제 승인 - 잘못 요청된 공고")
    void requestConfirmFailPostId() {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";
        Long paymentId = 10000L;

        PaymentService paymentService = new PaymentService(paymentRepository, null, null);

        // 예외 발생 404
        assertThrows(NotFoundException.class, () -> {
            paymentService.confirm(new PaymentConfirmRequest(paymentKey, orderId, amount), paymentId, savedUser1Id, amount);
        });
    }

    @Test
    @DisplayName("[PaymentController] 결제 승인 - 토스 결제 승인 실패")
    void requestConfirmFailConfirm() {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";

        TossPaymentClient mockTossPaymentClient = mock(TossPaymentClient.class);
        when(mockTossPaymentClient.callApiConfirm(any(), any(), any())).thenThrow(new InvalidException(11, "결제 승인에 실패했습니다."));

        PaymentService paymentService = new PaymentService(paymentRepository, paymentPersistenceService, mockTossPaymentClient);

        // 예외 발생 400번
        assertThrows(InvalidException.class, () -> {
            paymentService.confirm(new PaymentConfirmRequest(paymentKey, orderId, amount), savedPayment1Id, savedUser1Id, amount);
        });
    }

    private static @NonNull RestClient mockRestClient(HttpStatus httpStatus) {
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(new ResponseEntity<>(httpStatus));
        return restClient;
    }

    @Test
    @DisplayName("[PaymentController] 결제 정보 임시 저장")
    void tempSave() throws Exception {

        String orderId = UUID.randomUUID().toString();
        String amount = "10000";

        ResultActions resultActions = mvc.perform(
                post("/api/v1/payments/saveAmount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(accessTokenCookie1)
                        .session(new MockHttpSession())
                        .content("""
                                {
                                    "orderId": "%s",
                                    "amount": "%s"
                                }
                                """.formatted(orderId, amount))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(PaymentController.class))
                .andExpect(handler().methodName("tempSave"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-n"))
                .andExpect(jsonPath("$.msg").value("결제 정보 임시 저장에 성공했습니다."));
    }

    @Test
    @DisplayName("[PaymentController] 결제 정보 임시 저장 검증 - 성공")
    void verifyAmount() throws Exception {

        String orderId = UUID.randomUUID().toString();
        String amount = "10000";
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("orderId", orderId);
        session.setAttribute("amount", amount);

        ResultActions resultActions = mvc.perform(
                post("/api/v1/payments/verifyAmount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(accessTokenCookie1)
                        .session(session)
                        .content("""
                                {
                                    "orderId": "%s",
                                    "amount": "%s"
                                }
                                """.formatted(orderId, amount))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(PaymentController.class))
                .andExpect(handler().methodName("verifyAmount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-n"))
                .andExpect(jsonPath("$.msg").value("결제 정보가 유효합니다."));
    }

    @Test
    @DisplayName("[PaymentController] 결제 정보 임시 저장 검증 - 검증 실패")
    void verifyAmountFail() throws Exception {

        String orderId = UUID.randomUUID().toString();
        // 서버에 저장된 금액과 다른 금액을 입력
        String amount = "5000";
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("orderId", orderId);
        session.setAttribute("amount", "10000");

        ResultActions resultActions = mvc.perform(
                post("/api/v1/payments/verifyAmount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(accessTokenCookie1)
                        .session(session)
                        .content("""
                                {
                                    "orderId": "%s",
                                    "amount": "%s"
                                }
                                """.formatted(orderId, amount))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(PaymentController.class))
                .andExpect(handler().methodName("verifyAmount"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-10"))
                .andExpect(jsonPath("$.msg").value("결제 금액 정보가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("[PaymentController] 결제 데이터 가져오기")
    void paymentList() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/payments")
                        .cookie(accessTokenCookie1)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(PaymentController.class))
                .andExpect(handler().methodName("paymentList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-n"))
                .andExpect(jsonPath("$.msg").value("결제 정보를 불러왔습니다."))
                .andExpect(jsonPath("$.data[0]").exists())
                .andExpect(jsonPath("$.data[1]").exists());
    }

    @Test
    @DisplayName("[PaymentController] 결제 상세 데이터 가져오기 - 성공")
    void getPayment() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/payments/" + savedPayment1Id)
                        .cookie(accessTokenCookie1)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(PaymentController.class))
                .andExpect(handler().methodName("getPayment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-n"))
                .andExpect(jsonPath("$.msg").value("결제 정보를 불러왔습니다."))
                .andExpect(jsonPath("$.data.id").value(savedPayment1Id))
                .andExpect(jsonPath("$.data.amount").value(60_000))
                .andExpect(jsonPath("$.data.hourlyPaySnapshot").value(15_000))
                .andExpect(jsonPath("$.data.hours").value(4.0))
                .andExpect(jsonPath("$.data.orderId").isEmpty())
                .andExpect(jsonPath("$.data.paymentStatus").value("READY"));
    }

    @Test
    @DisplayName("[PaymentController] 결제 상세 데이터 가져오기 - 잘못된 결제 ID")
    void getPaymentFailInvalidPaymentId() throws Exception {
        Long paymentId = 10000L;

        ResultActions resultActions = mvc.perform(
                get("/api/v1/payments/" + paymentId)
                        .cookie(accessTokenCookie1)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(PaymentController.class))
                .andExpect(handler().methodName("getPayment"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404-10"))
                .andExpect(jsonPath("$.msg").value("결제 정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("[PaymentController] 결제 상세 데이터 가져오기 - 다른 회원이 조회")
    void getPaymentFailOtherMember() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/payments/" + savedPayment1Id)
                        .cookie(accessTokenCookie2)
                        .param("userId", String.valueOf(savedUser2Id))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(PaymentController.class))
                .andExpect(handler().methodName("getPayment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("400-10"))
                .andExpect(jsonPath("$.msg").value("사용자의 결제 정보가 아닙니다."));
    }

    @Test
    @DisplayName("[PaymentController] 결제 취소 - 성공")
    void requestCancel() {

        String cancelReason = "결제 취소 사유";

        TossPaymentClient mockTossPaymentClient = mock(TossPaymentClient.class);
        when(mockTossPaymentClient.callApiCancel(any(), any(), any())).thenReturn(ResponseEntity.ok(new TossConfirmResponse("계좌이체", "60000")));

        PaymentService paymentService = new PaymentService(paymentRepository, paymentPersistenceService, mockTossPaymentClient);

        Payment canceledPayment = paymentService.cancel(savedUser1Id, savedPayment1Id, new PaymentCancelRequest(cancelReason));

        assertEquals(PaymentStatus.CANCELED, canceledPayment.getPaymentStatus());
        assertEquals("결제 취소 사유", canceledPayment.getCancelReason());
        assertEquals(LocalDateTime.now().getHour(), canceledPayment.getCanceledAt().getHour());
        assertEquals(LocalDateTime.now().getMinute(), canceledPayment.getCanceledAt().getMinute());
        assertEquals(0, canceledPayment.getBalanceAmount());
    }

    @Test
    @DisplayName("[PaymentController] 결제 취소 - 토스 결제 취소 실패")
    void requestCancelFailCancel() {

        String cancelReason = "결제 취소 사유";

        TossPaymentClient mockTossPaymentClient = mock(TossPaymentClient.class);
        when(mockTossPaymentClient.callApiCancel(any(), any(), any())).thenThrow(InvalidException.class);

        PaymentService paymentService = new PaymentService(paymentRepository, paymentPersistenceService, mockTossPaymentClient);

        // 예외 발생 400번
        assertThrows(InvalidException.class, () -> {
            paymentService.cancel(savedUser1Id, savedPayment1Id, new PaymentCancelRequest(cancelReason));
        });
    }
}