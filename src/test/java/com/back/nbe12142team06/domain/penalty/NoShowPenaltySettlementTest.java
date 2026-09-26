package com.back.nbe12142team06.domain.penalty;

import com.back.nbe12142team06.domain.application.dto.ApplicationAcceptResponse;
import com.back.nbe12142team06.domain.application.dto.ApplicationApplyResponse;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.entity.EscortProgressLog;
import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.repository.EscortProgressLogRepository;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.domain.payment.client.TossPaymentClient;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.penalty.entity.NoShowPenalty;
import com.back.nbe12142team06.domain.penalty.repository.NoShowPenaltyRepository;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.ride.entity.RideSelect;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.service.UserService;
import jakarta.persistence.EntityManager;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class NoShowPenaltySettlementTest {

    // 시급
    private final int HOURLY_PAY = 15_000;
    // 동행 시간
    private final int HOURS = 4;
    // 의뢰인 결제 금액
    private final int PAYOUT_AMOUNT = HOURLY_PAY * HOURS;
    // 플랫폼 수수료
    private final int PLATFORM_FEE = (int) (PAYOUT_AMOUNT * 0.1);
    // 패널티 금액
    private final int PENALTY_AMOUNT = (int) ((PAYOUT_AMOUNT - PLATFORM_FEE) * 0.1);
    // 동행 매니저에게 정산되는 금액
    private final int SETTLEMENT_AMOUNT = PAYOUT_AMOUNT - PLATFORM_FEE - PENALTY_AMOUNT;

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
    private EscortProfileRepository escortProfileRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private NoShowPenaltyRepository noShowPenaltyRepository;
    @Autowired
    private EscortProgressLogRepository escortProgressLogRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @MockitoBean
    private TossPaymentClient tossPaymentClient;

    User client;
    User escort;
    Post post;
    Cookie clientAccessToken;
    Cookie escortAccessToken;
    Application application;

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

        client = userService.signUp(new UserSignUpRequest(
                username, password, email, name, Role.valueOf(roleClient),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                phoneNum, region));

        escort = userService.signUp(new UserSignUpRequest(
                username + "2", password + "2", email + "2", name + "2", Role.valueOf(roleEscort),
                Gender.valueOf(gender),
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
                "010-9999-9991", region));

        EscortProfile escortProfile1 = new EscortProfile(escort, "하이", "오픈은행", escort.getName(), "000-1234567-000");
        escortProfileRepository.save(escortProfile1);
        escortProfile1.verify(LocalDateTime.now()); // 교육 이수 처리 (지원 가능 상태)

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
        int hourlyPay = HOURLY_PAY;
        LocalDateTime recruitStartAt = LocalDateTime.now().plusDays(1);
        LocalDateTime recruitEndAt = LocalDateTime.now().plusDays(6);
        LocalDateTime escortStartAt = LocalDateTime.now().plusDays(7);
        LocalDateTime escortEndAt = LocalDateTime.now().plusDays(7).plusHours(HOURS);
        PostWriteRequest postWriteRequest = new PostWriteRequest(
                title, content, postRegion, hospitalName, hospitalAddress, hospitalLat, hospitalLng,
                pickupAddress, pickupLat, pickupLng, hourlyPay, recruitStartAt, recruitEndAt, escortStartAt, escortEndAt,
                RideSelect.TAXI, RideSelect.TAXI, "", true
        );

        // 지원 취소 후 패널티 적용
        PostWriteRequest tempPostWriteRequest = new PostWriteRequest(
                title, content, postRegion, hospitalName, hospitalAddress, hospitalLat, hospitalLng,
                pickupAddress, pickupLat, pickupLng, hourlyPay, recruitStartAt, recruitEndAt, escortStartAt, escortEndAt,
                RideSelect.TAXI, RideSelect.TAXI, "", true
        );
        applicationCancel(tempPostWriteRequest);

        // 지원 후 승인
        application(postWriteRequest);

        // user로 로그인해 인증 쿠키 확보
        clientAccessToken = mvc.perform(
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

        escortAccessToken = mvc.perform(
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

    private void application(PostWriteRequest postWriteRequest) {
        post = postService.findById(postService.write(client.getId(), postWriteRequest).id());

        ApplicationApplyResponse applyResponse = applicationService.apply(post.getId(), escort.getId());
        ApplicationAcceptResponse acceptResponse = applicationService.accept(applyResponse.id(), client.getId());

        application = applicationRepository.findAllByPostIdWithEscort(post.getId()).stream().findFirst().orElse(null);
    }

    private void applicationCancel(PostWriteRequest tempPostWriteRequest) {
        Post tempPost = postService.findById(postService.write(client.getId(), tempPostWriteRequest).id());
        ApplicationApplyResponse tempApplyResponse = applicationService.apply(tempPost.getId(), escort.getId());
        ApplicationAcceptResponse tempAcceptResponse = applicationService.accept(tempApplyResponse.id(), client.getId());
        Application tempApplication = applicationRepository.findAllByPostIdWithEscort(tempPost.getId()).stream().findFirst().orElse(null);
        applicationService.cancel(tempApplication.getId(), escort.getId());
    }

    @Test
    @DisplayName("[NoShowPenaltyService] 지원 승인 후 취소 패널티")
    void noShow() {
        // 패널티 적용 되었는지 검증
        EscortProfile escortProfile = escortProfileRepository.findByIdWithUser(escort.getId()).get();
        assertEquals(1, escortProfile.getNoShowCount());

        // 공고 결제 완료 처리
        paymentRepository.testStatusDone(post.getId());

        // 동행 시작 -> 동행 완료까지
        for (EscortProgress progress : EscortProgress.values()) {
            LocalDateTime occurredAt = LocalDateTime.now();
            // 출발 전에는 저장하지 않음
            if (progress.equals(EscortProgress.NOT_STARTED)) {
                continue;
            } else if (progress.equals(EscortProgress.DEPARTED)) {
                // 출발 시간에는 now()를 저장
                application.getPost().startProgress(occurredAt);
            } else {
                // 이후부터 단계마다 +1시간씩 저장, 기본은 2이므로 -1
                occurredAt = occurredAt.plusHours(progress.ordinal() - 1);
            }
            EscortProgressLog progressLog = EscortProgressLog.builder()
                    .application(application)
                    .progress(progress)
                    .occurredAt(occurredAt)
                    .build();
            escortProgressLogRepository.save(progressLog);
        }

        // 동행 완료 처리
        postService.escortComplete(post.getId(), client.getId());

        Settlement settlement = settlementRepository.findAll().stream().findFirst().get();
        NoShowPenalty noShowPenalty = noShowPenaltyRepository.findAll().stream().findFirst().get();

        // 1. 실제 정산 금액 2. 패널티 금액 3. 플랫폼 수수료
        assertEquals(SETTLEMENT_AMOUNT, settlement.getPayoutAmount());
        assertEquals(PENALTY_AMOUNT, settlement.getPenaltyAmount());
        assertEquals(PLATFORM_FEE, settlement.getPlatformFee());
        // 1. 패널티 차감 전 금액 2. 패널티 금액
        assertEquals(PAYOUT_AMOUNT - PLATFORM_FEE, noShowPenalty.getBaseAmount());
        assertEquals(PENALTY_AMOUNT, noShowPenalty.getAmount());
    }

}
