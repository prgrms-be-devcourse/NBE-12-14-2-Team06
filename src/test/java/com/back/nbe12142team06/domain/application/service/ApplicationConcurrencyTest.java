package com.back.nbe12142team06.domain.application.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.repository.EscortProgressLogRepository;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationConcurrencyTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EscortProfileRepository escortProfileRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EscortProgressLogRepository escortProgressLogRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long postId;
    private Long escortId;
    private Long clientId;

    private static final AtomicInteger TEST_SEQUENCE = new AtomicInteger();

    @BeforeEach
    void setUp() {

        applicationRepository.deleteAll();

        int seq = TEST_SEQUENCE.incrementAndGet();

        User client = new User(
                "concurrencyClient" + seq,
                passwordEncoder.encode("testPassword"),
                "concurrencyClient" + seq + "@test.com",
                "의뢰인",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                String.format("010-1111-%04d", seq),
                "수원"
        );

        client = userRepository.save(client);
        clientId = client.getId();

        User escort = new User(
                "concurrencyEscort" + seq,
                passwordEncoder.encode("testPassword"),
                "concurrencyEscort" + seq + "@test.com",
                "동행인",
                Role.ESCORT,
                Gender.FEMALE,
                LocalDate.of(1995, 1, 1),
                String.format("010-2222-%04d", seq),
                "수원"
        );

        escort = userRepository.save(escort);
        escortId = escort.getId();

        saveVerifiedProfile(escort, "1234");

        Post post = Post.builder()
                .client(client)
                .title("동시성 테스트 공고")
                .content("동시 지원 테스트")
                .region("수원")
                .hospitalName("테스트 병원")
                .hospitalAddress("경기도 수원시")
                .hospitalLat(BigDecimal.valueOf(37.2795))
                .hospitalLng(BigDecimal.valueOf(127.0476))
                .pickupAddress("경기도 수원시")
                .pickupLat(BigDecimal.valueOf(37.2636))
                .pickupLng(BigDecimal.valueOf(127.0286))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now().plusDays(1))
                .escortStartAt(LocalDateTime.now().plusDays(2))
                .escortEndAt(LocalDateTime.now().plusDays(2).plusHours(3))
                .patientNote("테스트")
                .reportRequired(false)
                .build();

        postId = postRepository.save(post).getId();

        Payment payment = Payment.builder()
                .amount(post.getTotalPay().intValue())
                .hourlyPaySnapshot(post.getHourlyPay())
                .hours(post.getEscortHours())
                .post(post)
                .paymentStatus(PaymentStatus.DONE)
                .build();

        paymentRepository.save(payment);
    }

    private void saveVerifiedProfile(User escort, String accountNumber) {
        escortProfileRepository.save(
                new EscortProfile(escort, "동행인 소개", "테스트은행", escort.getName(), accountNumber));

        transactionTemplate.executeWithoutResult(status ->
                escortProfileRepository.verify(escort.getId(), LocalDateTime.now()));
    }


    @Test
    @DisplayName("동일한 동행인이 같은 공고에 동시에 지원")
    void concurrentApply() throws InterruptedException {

        int threadCount = 10;

        ExecutorService executorService =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch =
                new CountDownLatch(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch doneLatch =
                new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {

            executorService.submit(() -> {

                readyLatch.countDown();

                try {
                    startLatch.await();

                    applicationService.apply(postId, escortId);

                } catch (Exception e) {
                    System.out.println(
                            Thread.currentThread().getName()
                                    + " 실패: "
                                    + e.getClass().getSimpleName()
                                    + " / "
                                    + e.getMessage()
                    );
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 모든 스레드가 준비될 때까지 대기
        readyLatch.await();

        // 동시에 출발
        startLatch.countDown();

        // 모든 요청 종료까지 대기
        doneLatch.await();

        long count = applicationRepository.count();

        System.out.println("저장된 지원 개수 = " + count);

        assertEquals(1, count);

        executorService.shutdown();
    }

    @Test
    @DisplayName("같은 공고의 서로 다른 지원자를 동시에 승인")
    void concurrentAccept() throws InterruptedException {

        int seq = TEST_SEQUENCE.incrementAndGet();

        User escort2 = new User(
                "concurrencyEscort2_" + seq,
                passwordEncoder.encode("testPassword"),
                "concurrencyEscort2_" + seq + "@test.com",
                "동행인2",
                Role.ESCORT,
                Gender.MALE,
                LocalDate.of(1996, 1, 1),
                String.format("010-3333-%04d", seq),
                "수원"
        );

        escort2 = userRepository.save(escort2);

        saveVerifiedProfile(escort2, "5678");

        // 기존 동행인 조회
        User escort1 = userRepository.findById(escortId)
                .orElseThrow();

        Post post = postRepository.findById(postId)
                .orElseThrow();

        // 같은 공고에 서로 다른 동행인 2명이 지원한 상태 생성
        Application application1 = Application.builder()
                .post(post)
                .escort(escort1)
                .build();

        Application application2 = Application.builder()
                .post(post)
                .escort(escort2)
                .build();

        application1 = applicationRepository.save(application1);
        application2 = applicationRepository.save(application2);

        Long applicationId1 = application1.getId();
        Long applicationId2 = application2.getId();

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch =
                new CountDownLatch(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch doneLatch =
                new CountDownLatch(2);

        // 지원자 1 승인
        executorService.submit(() -> {

            readyLatch.countDown();

            try {
                startLatch.await();

                applicationService.accept(
                        applicationId1,
                        clientId
                );

            } catch (Exception e) {
                System.out.println(
                        Thread.currentThread().getName()
                                + " 승인 실패: "
                                + e.getClass().getSimpleName()
                                + " / "
                                + e.getMessage()
                );
            } finally {
                doneLatch.countDown();
            }
        });

        // 지원자 2 승인
        executorService.submit(() -> {

            readyLatch.countDown();

            try {
                startLatch.await();

                applicationService.accept(
                        applicationId2,
                        clientId
                );

            } catch (Exception e) {
                System.out.println(
                        Thread.currentThread().getName()
                                + " 승인 실패: "
                                + e.getClass().getSimpleName()
                                + " / "
                                + e.getMessage()
                );
            } finally {
                doneLatch.countDown();
            }
        });

        // 두 스레드 준비
        readyLatch.await();

        // 동시에 승인 시작
        startLatch.countDown();

        // 둘 다 종료될 때까지 기다림
        doneLatch.await();

        // 최종 DB 상태 확인
        Post resultPost = postRepository.findById(postId)
                .orElseThrow();

        long acceptedCount =
                applicationRepository
                        .findAllByPostAndStatus(
                                resultPost,
                                ApplicationStatus.ACCEPTED
                        )
                        .size();

        System.out.println(
                "최종 승인된 지원자 수 = " + acceptedCount
        );

        assertEquals(1, acceptedCount);

        executorService.shutdown();
    }

    @Test
    @DisplayName("같은 지원에 승인과 취소가 동시에 요청")
    void concurrentAcceptAndCancel() throws InterruptedException {

        // 테스트용 지원 1건 생성
        User escort = userRepository.findById(escortId)
                .orElseThrow();

        Post post = postRepository.findById(postId)
                .orElseThrow();

        Application application = Application.builder()
                .post(post)
                .escort(escort)
                .build();

        application = applicationRepository.save(application);

        Long applicationId = application.getId();

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch =
                new CountDownLatch(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch doneLatch =
                new CountDownLatch(2);

        // 승인 요청
        executorService.submit(() -> {

            readyLatch.countDown();

            try {
                startLatch.await();

                applicationService.accept(
                        applicationId,
                        clientId
                );

            } catch (Exception e) {
                System.out.println(
                        Thread.currentThread().getName()
                                + " 승인 실패: "
                                + e.getClass().getSimpleName()
                                + " / "
                                + e.getMessage()
                );
            } finally {
                doneLatch.countDown();
            }
        });

        // 취소 요청
        executorService.submit(() -> {

            readyLatch.countDown();

            try {
                startLatch.await();

                applicationService.cancel(
                        applicationId,
                        escortId
                );

            } catch (Exception e) {
                System.out.println(
                        Thread.currentThread().getName()
                                + " 취소 실패: "
                                + e.getClass().getSimpleName()
                                + " / "
                                + e.getMessage()
                );
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await();

        startLatch.countDown();

        doneLatch.await();

        Application resultApplication =
                applicationRepository.findById(applicationId)
                        .orElseThrow();

        Post resultPost =
                postRepository.findById(postId)
                        .orElseThrow();

        System.out.println(
                "최종 지원 상태 = "
                        + resultApplication.getStatus()
        );

        System.out.println(
                "최종 공고 상태 = "
                        + resultPost.getPostStatus()
        );

        /*
         * 정상적으로 가능한 조합인지 확인
         *
         * 1. 취소가 먼저 처리됨
         *    Application = CANCELED
         *    Post = OPEN
         *
         * 2. 승인이 먼저 처리되고 이후 취소됨
         *    Application = NO_SHOW
         *    Post = OPEN
         *
         * 3. 승인이 완료되고 취소 요청이 실패
         *    Application = ACCEPTED
         *    Post = MATCHED
         */

        boolean validState =
                (resultApplication.getStatus() == ApplicationStatus.CANCELED
                        && resultPost.getPostStatus() == PostStatus.OPEN)

                        || (resultApplication.getStatus() == ApplicationStatus.NO_SHOW
                        && resultPost.getPostStatus() == PostStatus.OPEN)

                        || (resultApplication.getStatus() == ApplicationStatus.ACCEPTED
                        && resultPost.getPostStatus() == PostStatus.MATCHED);

        assertTrue(
                validState,
                "지원 상태와 공고 상태가 일치하지 않습니다."
        );

        executorService.shutdown();
    }

    @Test
    @DisplayName("같은 진행 상태 변경 요청이 동시에 들어오면 중복 로그가 생성되지 않아야 한다")
    void concurrentProgressUpdate() throws InterruptedException {

        User escort = userRepository.findById(escortId)
                .orElseThrow();

        Post post = postRepository.findById(postId)
                .orElseThrow();

        Application application = Application.builder()
                .post(post)
                .escort(escort)
                .build();

        application.accept();
        application = applicationRepository.save(application);

        Long applicationId = application.getId();

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch =
                new CountDownLatch(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch doneLatch =
                new CountDownLatch(2);

        for (int i = 0; i < 2; i++) {

            executorService.submit(() -> {

                readyLatch.countDown();

                try {
                    startLatch.await();

                    applicationService.updateProgress(
                            applicationId,
                            escortId,
                            EscortProgress.DEPARTED
                    );

                } catch (Exception e) {
                    System.out.println(
                            Thread.currentThread().getName()
                                    + " 진행 상태 변경 실패: "
                                    + e.getClass().getSimpleName()
                                    + " / "
                                    + e.getMessage()
                    );
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        long progressLogCount =
                escortProgressLogRepository.count();

        System.out.println(
                "저장된 진행 로그 개수 = " + progressLogCount
        );

        assertEquals(1, progressLogCount);

        executorService.shutdown();
    }
}
