package com.back.nbe12142team06.domain.payment.controller;

import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
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
import jakarta.persistence.EntityManager;
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
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
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
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager entityManager;

    private Long savedUser1Id;
    private Long savedUser2Id;
    private Long savedPostId;

    @BeforeEach
    public void init() {
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
                phoneNum, region));
        savedUser2Id = user2.getId();

        PostWriteRequest postWriteRequest = new PostWriteRequest(
                "정형외과 동행 구합니다",
                "무릎 수술 후 검진 예약이 있어 동행인이 필요합니다.",
                "서울",
                "서울성모병원",
                "서울 서초구 반포대로 222",
                BigDecimal.valueOf(37.5012743),
                BigDecimal.valueOf(127.0051893),
                "서울 서초구 잠원동 10-1",
                BigDecimal.valueOf(37.5160000),
                BigDecimal.valueOf(127.0200000),
                15_000,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(7).plusHours(4),
                LocalDateTime.now().plusDays(6),
                "",
                true
        );

        Post post = postService.write(user1, postWriteRequest);
        savedPostId = post.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("[PaymentController] 결제 승인 - 성공")
    void requestConfirm() {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";

        RestClient restClient = mockRestClient(HttpStatus.OK);

        PaymentService paymentService = new PaymentService(paymentRepository, objectMapper, restClient);

        Payment payment = paymentService.confirm(new PaymentConfirmRequest(paymentKey, orderId, amount), savedPostId, savedUser1Id);

        assertEquals(PaymentStatus.DONE, payment.getPaymentStatus());
        assertEquals(LocalDateTime.now().getHour(), payment.getApprovedAt().getHour());
        assertEquals(LocalDateTime.now().getMinute(), payment.getApprovedAt().getMinute());
    }

    @Test
    @DisplayName("[PaymentController] 결제 승인 - 잘못 요청된 유저")
    void requestConfirmFailUserId() {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";

        PaymentService paymentService = new PaymentService(paymentRepository, null, null);

        // 예외 발생 400번
        assertThrows(InvalidException.class, () -> {
            paymentService.confirm(new PaymentConfirmRequest(paymentKey, orderId, amount), savedPostId, savedUser2Id);
        });
    }

    @Test
    @DisplayName("[PaymentController] 결제 승인 - 잘못 요청된 공고")
    void requestConfirmFailPostId() {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";
        Long invalidPostId = 10L;

        PaymentService paymentService = new PaymentService(paymentRepository, null, null);

        // 예외 발생 400번
        assertThrows(InvalidException.class, () -> {
            paymentService.confirm(new PaymentConfirmRequest(paymentKey, orderId, amount), invalidPostId, savedUser1Id);
        });
    }

    @Test
    @DisplayName("[PaymentController] 결제 승인 - 토스 결제 승인 실패")
    void requestConfirmFailConfirm() {

        String paymentKey = "temp";
        String orderId = "temp";
        String amount = "10000";
        Long postId = 10L;
        Long userId = 1L;

        RestClient restClient = mockRestClient(HttpStatus.INTERNAL_SERVER_ERROR);

        PaymentService paymentService = new PaymentService(paymentRepository, objectMapper, restClient);

        // 예외 발생 400번
        assertThrows(InvalidException.class, () -> {
            paymentService.confirm(new PaymentConfirmRequest(paymentKey, orderId, amount), postId, userId);
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
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(httpStatus));
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
}