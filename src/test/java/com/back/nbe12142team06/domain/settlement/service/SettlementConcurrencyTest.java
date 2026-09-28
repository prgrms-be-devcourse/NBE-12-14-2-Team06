package com.back.nbe12142team06.domain.settlement.service;

import com.back.nbe12142team06.DatabaseCleaner;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.penalty.repository.NoShowPenaltyRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.settlement.client.SettlementClient;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientResponse;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class SettlementConcurrencyTest {

    private static final int PAYOUT_AMOUNT = 60_000;
    private static final int SETTLEMENT_AMOUNT = (int) (PAYOUT_AMOUNT * 0.9);

    @Autowired
    private SettlementService settlementService;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private NoShowPenaltyRepository noShowPenaltyRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EscortProfileRepository escortProfileRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @MockitoBean
    private SettlementClient settlementClient;

    private User client;
    private User escort;

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @BeforeEach
    void setUp() {
        noShowPenaltyRepository.deleteAll();
        settlementRepository.deleteAll();

        String tag = String.valueOf(System.nanoTime());
        String phoneTag = tag.substring(tag.length() - 7);

        client = userRepository.save(User.builder()
                .username("client-" + tag)
                .password("password1!")
                .email("client-" + tag + "@test.com")
                .name("의뢰인이름")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1970, 1, 1))
                .phoneNum("010" + phoneTag + "0")
                .region("서울")
                .build());

        escort = userRepository.save(User.builder()
                .username("escort-" + tag)
                .password("password1!")
                .email("escort-" + tag + "@test.com")
                .name("동행매니저이름")
                .role(Role.ESCORT)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNum("010" + phoneTag + "1")
                .region("서울")
                .build());

        EscortProfile profile = new EscortProfile(escort, "자기소개", "오픈은행", "동행매니저이름", "000-1234567-000");
        EscortProfile savedProfile = escortProfileRepository.save(profile);
        escortProfileRepository.verify(savedProfile.getUserId(), LocalDateTime.now());
    }

    @Test
    @DisplayName("[SettlementService] 동시성 스케줄러와 정산 요청")
    void concurrency1() throws Exception {

        Post post = savePost("정산 경합 공고", PostStatus.COMPLETED);
        Application application = saveApplication(post, ApplicationStatus.ACCEPTED, post.getId());
        Settlement settlement = saveSettlement(application, 0);

        when(settlementClient.settlementRequest(any()))
                .thenAnswer(invocation -> {
                    Thread.sleep(500);
                    return new SettlementClientResponse("000-1234567-000", "동행매니저이름", SETTLEMENT_AMOUNT);
                });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        executor.submit(() -> {
            start.await();
            settlementService.request(escort.getId(), settlement.getId());
            return null;
        });
        executor.submit(() -> {
            start.await();
            settlementService.settlementProcess();
            return null;
        });
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Mockito.verify(settlementClient, times(1)).settlementRequest(any());
    }

    private Post savePost(String title, PostStatus status) {
        return postRepository.save(Post.builder()
                .client(client)
                .title(title)
                .content("병원 동행 테스트")
                .region("서울")
                .hospitalName("서울아산병원")
                .hospitalAddress("서울특별시 송파구 올림픽로43길 88")
                .hospitalLat(BigDecimal.valueOf(37.52643))
                .hospitalLng(BigDecimal.valueOf(127.1096))
                .pickupAddress("서울특별시 중구 세종대로 지하2")
                .pickupLat(BigDecimal.valueOf(37.555800))
                .pickupLng(BigDecimal.valueOf(126.972000))
                .hourlyPay(15_000)
                .recruitStartAt(LocalDateTime.now().minusDays(5))
                .recruitEndAt(LocalDateTime.now().minusDays(4))
                .escortStartAt(LocalDateTime.now().minusDays(2))
                .escortEndAt(LocalDateTime.now().minusDays(2).plusHours(4))
                .postStatus(status)
                .build());
    }

    private Application saveApplication(Post post, ApplicationStatus status, Long acceptedPostId) {
        return applicationRepository.save(Application.builder()
                .post(post)
                .escort(escort)
                .status(status)
                .acceptedPostId(acceptedPostId)
                .build());
    }

    private Settlement saveSettlement(Application application, int penaltyAmount) {
        return settlementRepository.save(Settlement.builder()
                .payoutAmount(SETTLEMENT_AMOUNT - penaltyAmount)
                .platformFee(PAYOUT_AMOUNT - SETTLEMENT_AMOUNT)
                .penaltyAmount(penaltyAmount)
                .settledDate(LocalDate.now())
                .application(application)
                .escort(escort)
                .build());
    }
}
