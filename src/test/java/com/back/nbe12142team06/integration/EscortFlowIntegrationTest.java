package com.back.nbe12142team06.integration;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.entity.EscortProgressLog;
import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.repository.EscortProgressLogRepository;
import com.back.nbe12142team06.domain.education.entity.EducationVideo;
import com.back.nbe12142team06.domain.education.repository.EducationVideoRepository;
import com.back.nbe12142team06.domain.payment.client.TossPaymentClient;
import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [통합] 가입부터 정산까지 실제 HTTP 요청으로 전 과정을 검증한다..
 *
 * 지금까지의 테스트는 대부분 @Transactional 이 붙은 단위/컨트롤러 테스트라서
 * "테스트는 통과하는데 실제 요청은 터지는" 버그를 놓쳐왔다 (LazyInitializationException,
 * tinytext 잘림 등이 그렇게 빠져나갔다 — @Transactional 로 감싸면 컨트롤러가 반환한 뒤에도
 * 영속성 컨텍스트/세션이 살아있어 지연 로딩이 조용히 성공해버리기 때문이다).
 *
 * 그래서 이 클래스에는 클래스에도 메서드에도 @Transactional 을 붙이지 않는다.
 * 각 스텝이 실제 서비스처럼 자기 트랜잭션을 커밋하고 끝나야, open-in-view: false 환경에서
 * 응답을 JSON 으로 직렬화하는 시점에 지연 로딩을 건드리면 실제로 터지는지 확인할 수 있다.
 * 대신 @Transactional 이 없어서 커밋된 데이터가 테스트 실행 사이에 남는다 — username /
 * email / phoneNum 을 System.nanoTime() 기반으로 고유하게 만들어 충돌을 피한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EscortFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private EducationVideoRepository educationVideoRepository;
    @Autowired
    private EscortProfileRepository escortProfileRepository;
    @Autowired
    private EscortProgressLogRepository escortProgressLogRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private TossPaymentClient tossPaymentClient;

    @BeforeEach
    void stubToss() {
        // 실제 토스 API 를 호출하지 않는다. confirm/cancel 둘 다 스텁해 두지만,
        // 시나리오 1 은 계획 동행 시간과 실제 동행 시간을 일치시켜(아래 옵션 B 참고)
        // 두 스텁 모두 호출되지 않는 것까지 검증한다.
        given(tossPaymentClient.callApiConfirm(any(), any(), any()))
                .willReturn(ResponseEntity.ok(new TossConfirmResponse("카드", "30000")));
        given(tossPaymentClient.callApiCancel(any(), any(), any()))
                .willReturn(ResponseEntity.ok(new TossConfirmResponse("카드", "30000")));
    }

    // ── 공용 헬퍼 ──────────────────────────────────────────────

    private String tag() {
        return String.valueOf(System.nanoTime());
    }

    private String phone(String tag, int seq) {
        String last7 = tag.substring(tag.length() - 7);
        return "010" + last7 + seq;
    }

    // UserResponse(/api/v1/users/profile 조회용)에는 id 가 없어서, 가입 응답
    // (UserSignUpResponse)에서 바로 회원 id 를 받아둔다.
    private Long signUp(String username, String password, String email, String name, String role, String phoneNum) throws Exception {
        String body = """
                {
                    "username": "%s",
                    "password": "%s",
                    "email": "%s",
                    "name": "%s",
                    "role": "%s",
                    "gender": "MALE",
                    "birthDate": "1990-01-01",
                    "phoneNum": "%s",
                    "region": "서울"
                }
                """.formatted(username, password, email, name, role, phoneNum);

        String response = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-1"))
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.parse(response).read("$.data.id")).longValue();
    }

    private Cookie login(String username, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("accessToken");
    }

    private void createClientProfile(Cookie clientCookie) throws Exception {
        mvc.perform(post("/api/v1/users/profile/client")
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "emergencyContactName": "보호자",
                                    "emergencyContactPhone": "010-9999-0000",
                                    "careNote": "특이사항 없음"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-2"));
    }

    private void createEscortProfile(Cookie escortCookie) throws Exception {
        mvc.perform(post("/api/v1/users/profile/escort")
                        .cookie(escortCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "intro": "안전하게 동행하겠습니다.",
                                    "bankName": "국민은행",
                                    "accountHolder": "동행인",
                                    "accountNumber": "123-456-7890"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-3"));
    }

    // 동행인 프로필의 verified 를 리포지토리로 직접 켠다. 승인/자동거절, 권한 경계처럼
    // 교육 이수 자체가 초점이 아닌 시나리오에서, 매번 영상 시청 플로우를 반복하지
    // 않기 위한 지름길이다. 교육 이수 플로우 자체는 시나리오 1에서 실제 API로 검증한다.
    private void verifyEscortDirectly(Long escortId) {
        transactionTemplate.executeWithoutResult(status ->
                escortProfileRepository.verify(escortId, LocalDateTime.now()));
    }

    private String postJson(LocalDateTime recruitStartAt, LocalDateTime recruitEndAt,
                             LocalDateTime escortStartAt, LocalDateTime escortEndAt, int hourlyPay) {
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
                    "hourlyPay": %d,
                    "recruitStartAt": "%s",
                    "recruitEndAt": "%s",
                    "escortStartAt": "%s",
                    "escortEndAt": "%s",
                    "rideSelectToHospital": "TAXI",
                    "rideSelectToHome": "TAXI",
                    "patientNote": "거동이 불편하신 70대 어르신",
                    "reportRequired": true
                }
                """.formatted(hourlyPay, recruitStartAt, recruitEndAt, escortStartAt, escortEndAt);
    }

    // ── 시나리오 1 ─────────────────────────────────────────────

    @Test
    @DisplayName("[통합] 의뢰인 가입부터 정산 데이터 생성까지 전 과정이 동작한다")
    void 의뢰인_가입부터_정산까지_전_과정() throws Exception {

        String tag = tag();
        int hourlyPay = 15000;

        // 1. 의뢰인 회원가입
        String clientUsername = "flow-client-" + tag;
        String clientPassword = "testPassword";
        signUp(clientUsername, clientPassword, "flow-client-" + tag + "@test.com", "의뢰인", "CLIENT", phone(tag, 1));

        // 2. 의뢰인 로그인 -> accessToken 쿠키 확보
        Cookie clientCookie = login(clientUsername, clientPassword);

        // 3. 의뢰인 프로필 등록
        createClientProfile(clientCookie);

        // 4. 동행인 회원가입 -> 로그인 -> 프로필 등록
        String escortUsername = "flow-escort-" + tag;
        String escortPassword = "testPassword";
        Long escortUserId = signUp(escortUsername, escortPassword, "flow-escort-" + tag + "@test.com", "동행인", "ESCORT", phone(tag, 2));
        Cookie escortCookie = login(escortUsername, escortPassword);

        // 교육 영상 진행 상황은 프로필 생성 시점에 존재하는 영상들에 대해서만 만들어지므로,
        // 프로필을 만들기 전에 필수 영상을 먼저 준비해 둔다.
        EducationVideo video = educationVideoRepository.save(
                new EducationVideo("교육 영상", "https://example.com/video.mp4", 1, true));

        createEscortProfile(escortCookie);

        // 5. 동행인 교육 이수 처리 (실제 시청 기록 API 사용)
        // durationSec=1, positionSec=1 로 첫 하트비트 한 번에 완료 처리되도록 맞춘다
        // (EducationProgress.record() 는 최초 호출 시 lastReceivedAt 이 없어 허용 오차가
        // 작다 - 자세한 계산은 EducationProgress 참고).
        mvc.perform(post("/api/v1/education-videos/{videoId}/watchlogs", video.getId())
                        .cookie(escortCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"positionSec": 1.0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-101"));

        assertThat(escortProfileRepository.findById(escortUserId).orElseThrow().getVerified()).isTrue();

        // 6. 의뢰인이 공고 등록 (동행 예정 시간 2시간)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recruitStartAt = now.plusDays(1);
        LocalDateTime recruitEndAt = recruitStartAt.plusHours(1);
        LocalDateTime escortStartAt = recruitEndAt.plusHours(1);
        LocalDateTime escortEndAt = escortStartAt.plusHours(2);

        String writeResponse = mvc.perform(post("/api/v1/posts")
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson(recruitStartAt, recruitEndAt, escortStartAt, escortEndAt, hourlyPay)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-11"))
                .andReturn().getResponse().getContentAsString();

        DocumentContext writeCtx = JsonPath.parse(writeResponse);
        Long postId = ((Number) writeCtx.read("$.data.id")).longValue();
        Long paymentId = ((Number) writeCtx.read("$.data.paymentId")).longValue();
        assertThat(postId).isNotNull();
        assertThat(paymentId).isNotNull();

        // [발견한 문제] PaymentRepository.findByPostId() 는 paymentStatus 가 DONE
        // 또는 PARTIAL_CANCELED 인 결제만 찾는다. 공고 등록 시 만들어지는 최초 결제는
        // READY 상태라 결제 승인(confirm)을 거치지 않으면 escortComplete() 의
        // validPayment() 가 "결제된 금액이 0원"으로 보고 매번 추가 결제 플로우를 탄다
        // (계획/실제 동행 시간이 같아도 소용없다). 그래서 실제 결제 승인 플로우
        // (금액 임시 저장 -> 승인)를 그대로 호출해 결제를 DONE 으로 만든다.
        String plannedAmount = String.valueOf(hourlyPay * 2);
        MockHttpSession paymentSession = new MockHttpSession();
        mvc.perform(post("/api/v1/payments/save-amount")
                        .session(paymentSession)
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId": "order-%s", "amount": "%s"}
                                """.formatted(tag, plannedAmount)))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/payments/{paymentId}/confirm", paymentId)
                        .session(paymentSession)
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paymentKey": "test-payment-key-%s", "orderId": "order-%s", "amount": "%s"}
                                """.formatted(tag, tag, plannedAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-41"));

        // 7. 동행인이 공고에 지원
        String applyResponse = mvc.perform(post("/api/v1/applications/{postId}", postId)
                        .cookie(escortCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-21"))
                .andReturn().getResponse().getContentAsString();
        Long applicationId = ((Number) JsonPath.parse(applyResponse).read("$.data.id")).longValue();

        // 8. 의뢰인이 지원 목록 조회 -> 방금 지원한 동행인이 1건 보이는지 확인
        mvc.perform(get("/api/v1/applications/posts/{postId}", postId).cookie(clientCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].applicationId").value(applicationId))
                .andExpect(jsonPath("$.data.content[0].escortId").value(escortUserId));

        // 9. 의뢰인이 지원 승인 -> Application ACCEPTED, Post MATCHED
        mvc.perform(patch("/api/v1/applications/{applicationId}/accept", applicationId).cookie(clientCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        assertThat(postRepository.findById(postId).orElseThrow().getPostStatus()).isEqualTo(PostStatus.MATCHED);

        // 10. 동행인이 진행 상태를 순서대로 변경
        mvc.perform(patch("/api/v1/applications/{applicationId}/progress", applicationId)
                        .cookie(escortCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"progress": "DEPARTED"}
                                """))
                .andExpect(status().isOk());

        // DEPARTED 시점에 Post 가 IN_PROGRESS 로 바뀐다
        assertThat(postRepository.findById(postId).orElseThrow().getPostStatus()).isEqualTo(PostStatus.IN_PROGRESS);

        // 순서를 어기면(TO_HOSPITAL 을 건너뛰고 AT_HOSPITAL) 400
        mvc.perform(patch("/api/v1/applications/{applicationId}/progress", applicationId)
                        .cookie(escortCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"progress": "AT_HOSPITAL"}
                                """))
                .andDo(print())
                .andExpect(status().isBadRequest());

        for (EscortProgress step : List.of(
                EscortProgress.TO_HOSPITAL, EscortProgress.AT_HOSPITAL,
                EscortProgress.TO_HOME, EscortProgress.ARRIVED_HOME)) {
            mvc.perform(patch("/api/v1/applications/{applicationId}/progress", applicationId)
                            .cookie(escortCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"progress": "%s"}
                                    """.formatted(step)))
                    .andExpect(status().isOk());
        }

        // [함정] DEPARTED 와 ARRIVED_HOME 이 실제로는 몇 밀리초 차이라 escortHours 가 0에
        // 가깝게 계산된다. 실서비스라면 동행이 몇 시간 걸리므로 문제가 안 되지만, 테스트에서
        // 그대로 두면 결제 취소 플로우(추가/부분 취소)를 타게 되어 정산 금액 검증이 무의미해진다.
        // (A) 그대로 두고 0원 처리/결제 취소를 검증할 수도 있었지만, 이 테스트는 정산 금액까지
        // 의미 있게 검증하는 (B) 를 택했다: DEPARTED 로그의 occurredAt 만 실제 시각(테스트 시각)에서
        // 2시간 전으로 되돌려, 계획한 동행 시간(2시간)과 실제 동행 시간(2시간)을 맞춘다.
        // main 코드는 건드리지 않고 테스트 데이터만 조정한다.
        Application application = applicationRepository.findById(applicationId).orElseThrow();
        LocalDateTime arrivedAt = escortProgressLogRepository
                .findByApplicationAndProgress(application, EscortProgress.ARRIVED_HOME)
                .orElseThrow()
                .getOccurredAt();
        EscortProgressLog departedLog = escortProgressLogRepository
                .findByApplicationAndProgress(application, EscortProgress.DEPARTED)
                .orElseThrow();
        escortProgressLogRepository.delete(departedLog);
        escortProgressLogRepository.saveAndFlush(
                EscortProgressLog.builder()
                        .application(application)
                        .progress(EscortProgress.DEPARTED)
                        .occurredAt(arrivedAt.minusHours(2))
                        .build()
        );

        // 11. 의뢰인이 동행 완료 처리
        // [발견한 문제] PostController.escortComplete() 는 RsData<PostDto> 를 선언하고도
        // 실제로는 new RsData<>("200-1", msg) 로 data 없이 반환한다 - $.data 가 항상 null.
        // 응답 바디로는 확인할 수 없어 상태는 리포지토리로 검증한다.
        mvc.perform(patch("/api/v1/posts/{postId}/escortComplete", postId).cookie(clientCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-18"));

        assertThat(postRepository.findById(postId).orElseThrow().getPostStatus()).isEqualTo(PostStatus.COMPLETED);
        assertThat(escortProfileRepository.findById(escortUserId).orElseThrow().getCompletedCount()).isEqualTo(1);

        // 계획한 동행 시간과 실제 동행 시간이 같으므로(옵션 B) 추가·부분 취소 플로우를 타지 않는다.
        // (결제 승인(confirm) 호출은 이미 있었으므로 취소(cancel)만 호출되지 않았는지 확인한다.)
        List<Payment> payments = paymentRepository.findByPostId(postId);
        assertThat(payments).hasSize(1);
        verify(tossPaymentClient, never()).callApiCancel(any(), any(), any());

        int totalPay = hourlyPay * 2; // 계획 2시간 == 실제 2시간(옵션 B)
        int expectedPayout = (int) (totalPay * 0.9);
        int expectedPlatformFee = totalPay - expectedPayout;

        Settlement settlement = settlementRepository.findAll().stream()
                .filter(s -> s.getEscort().getId().equals(escortUserId))
                .findFirst()
                .orElseThrow();
        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.PENDING);
        assertThat(settlement.getPayoutAmount()).isEqualTo(expectedPayout);
        assertThat(settlement.getPlatformFee()).isEqualTo(expectedPlatformFee);

        // 12. 동행인이 진료 보고서 작성
        mvc.perform(post("/api/v1/applications/{applicationId}/report", applicationId)
                        .cookie(escortCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "department": "ORTHOPEDICS",
                                    "purpose": "무릎 통증 검사",
                                    "originContent": "무릎 통증으로 내원하셨고 물리치료 처방을 받으셨습니다."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-51"));

        // 13. 의뢰인이 리뷰 작성
        mvc.perform(post("/api/v1/applications/{applicationId}/reviews", applicationId)
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201-61"));

        EscortProfile escortProfile = escortProfileRepository.findById(escortUserId).orElseThrow();
        assertThat(escortProfile.getRatingCount()).isEqualTo(1);
        assertThat(escortProfile.getRatingSum()).isEqualTo(5);

        // 14. 동행인 리뷰 목록 조회 -> 예전에 LazyInitializationException 으로 500 났던 지점
        // [발견한 문제] 컨트롤러 주석은 "리뷰는 공개 정보라 권한 체크 없음"이라고 되어 있지만,
        // SecurityConfig 에는 이 경로에 대한 permitAll 이 없어 "/api/**".authenticated() 에
        // 걸린다. 즉 실제로는 로그인(역할 무관)은 필요하다 - 완전 비로그인으로는 401 이 난다.
        mvc.perform(get("/api/v1/users/{userId}/reviews", escortUserId).cookie(escortCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200-61"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].rating").value(5))
                .andExpect(jsonPath("$.data[0].applicationId").value(applicationId));
    }

    // ── 시나리오 2 ─────────────────────────────────────────────

    @Test
    @DisplayName("[통합] 지원 승인 시 같은 공고의 나머지 지원자는 자동 거절되고 이후 지원은 막힌다")
    void 승인되면_나머지_지원자는_자동_거절된다() throws Exception {

        String tag = tag();

        String clientUsername = "flow2-client-" + tag;
        signUp(clientUsername, "testPassword", "flow2-client-" + tag + "@test.com", "의뢰인", "CLIENT", phone(tag, 1));
        Cookie clientCookie = login(clientUsername, "testPassword");

        LocalDateTime now = LocalDateTime.now();
        String writeResponse = mvc.perform(post("/api/v1/posts")
                        .cookie(clientCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson(now.plusDays(1), now.plusDays(1).plusHours(1),
                                now.plusDays(1).plusHours(2), now.plusDays(1).plusHours(4), 15000)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long postId = ((Number) JsonPath.parse(writeResponse).read("$.data.id")).longValue();

        // 동행인 2명이 지원
        String escort1Username = "flow2-escort1-" + tag;
        Long escort1Id = signUp(escort1Username, "testPassword", "flow2-escort1-" + tag + "@test.com", "동행인1", "ESCORT", phone(tag, 2));
        Cookie escort1Cookie = login(escort1Username, "testPassword");
        createEscortProfile(escort1Cookie);
        verifyEscortDirectly(escort1Id);

        String escort2Username = "flow2-escort2-" + tag;
        Long escort2Id = signUp(escort2Username, "testPassword", "flow2-escort2-" + tag + "@test.com", "동행인2", "ESCORT", phone(tag, 3));
        Cookie escort2Cookie = login(escort2Username, "testPassword");
        createEscortProfile(escort2Cookie);
        verifyEscortDirectly(escort2Id);

        String apply1Response = mvc.perform(post("/api/v1/applications/{postId}", postId).cookie(escort1Cookie))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long application1Id = ((Number) JsonPath.parse(apply1Response).read("$.data.id")).longValue();

        String apply2Response = mvc.perform(post("/api/v1/applications/{postId}", postId).cookie(escort2Cookie))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long application2Id = ((Number) JsonPath.parse(apply2Response).read("$.data.id")).longValue();

        // 의뢰인이 escort1 의 지원을 승인
        mvc.perform(patch("/api/v1/applications/{applicationId}/accept", application1Id).cookie(clientCookie))
                .andExpect(status().isOk());

        Application accepted = applicationRepository.findById(application1Id).orElseThrow();
        Application rejected = applicationRepository.findById(application2Id).orElseThrow();
        assertThat(accepted.getStatus()).isEqualTo(com.back.nbe12142team06.domain.application.enums.ApplicationStatus.ACCEPTED);
        assertThat(rejected.getStatus()).isEqualTo(com.back.nbe12142team06.domain.application.enums.ApplicationStatus.REJECTED);
        assertThat(postRepository.findById(postId).orElseThrow().getPostStatus()).isEqualTo(PostStatus.MATCHED);

        // 3번째 동행인이 지원하면 400 (모집 중인 공고만 지원 가능)
        String escort3Username = "flow2-escort3-" + tag;
        Long escort3Id = signUp(escort3Username, "testPassword", "flow2-escort3-" + tag + "@test.com", "동행인3", "ESCORT", phone(tag, 4));
        Cookie escort3Cookie = login(escort3Username, "testPassword");
        createEscortProfile(escort3Cookie);
        verifyEscortDirectly(escort3Id);

        mvc.perform(post("/api/v1/applications/{postId}", postId).cookie(escort3Cookie))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ── 시나리오 3 ─────────────────────────────────────────────
    // 기존 ApplicationControllerTest(t9: 다른 의뢰인의 공고 지원 목록 조회 403,
    // t11: 다른 의뢰인이 승인 시도 403, t30: 다른 동행인이 진행 상태 변경 시도 403)와
    // 기존 PostControllerTest(취소·동행완료 처리의 401/403 케이스)가 이미 검증하고 있어
    // 여기서는 다시 만들지 않는다. PostControllerTest 에 빠져 있던, "공고 등록" 자체에 대한
    // 인증/권한 경계 2건만 추가한다.

    @Test
    @DisplayName("[통합] 비로그인 상태로 공고 등록 시도 시 401-113 반환")
    void 비로그인_공고_등록_401() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        ResultActions resultActions = mvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson(now.plusDays(1), now.plusDays(1).plusHours(1),
                                now.plusDays(1).plusHours(2), now.plusDays(1).plusHours(4), 15000)))
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("401-113"));
    }

    @Test
    @DisplayName("[통합] 동행인 계정으로 공고 등록 시도 시 403 반환")
    void 동행인_공고_등록_403() throws Exception {

        String tag = tag();
        String escortUsername = "flow3-escort-" + tag;
        signUp(escortUsername, "testPassword", "flow3-escort-" + tag + "@test.com", "동행인", "ESCORT", phone(tag, 1));
        Cookie escortCookie = login(escortUsername, "testPassword");

        LocalDateTime now = LocalDateTime.now();
        ResultActions resultActions = mvc.perform(post("/api/v1/posts")
                        .cookie(escortCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson(now.plusDays(1), now.plusDays(1).plusHours(1),
                                now.plusDays(1).plusHours(2), now.plusDays(1).plusHours(4), 15000)))
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value("403-1"));
    }
}
