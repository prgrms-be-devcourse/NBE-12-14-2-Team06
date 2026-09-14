package com.back.nbe12142team06.domain.payment.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    @Test
    @DisplayName("[PaymentController] 결제 승인")
    void requestConfirm() {
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
    @DisplayName("[PaymentController] 결제 정보 임시 저장 검증")
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