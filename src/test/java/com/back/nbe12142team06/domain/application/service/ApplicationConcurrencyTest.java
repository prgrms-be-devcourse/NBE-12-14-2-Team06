package com.back.nbe12142team06.domain.application.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private Long postId;
    private Long escortId;
    private Long clientId;

    @BeforeEach
    void setUp() {

        applicationRepository.deleteAll();

        User client = new User(
                "concurrencyClient",
                passwordEncoder.encode("testPassword"),
                "concurrencyClient@test.com",
                "의뢰인",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                "010-1111-1111",
                "수원"
        );

        client = userRepository.save(client);
        clientId = client.getId();

        User escort = new User(
                "concurrencyEscort",
                passwordEncoder.encode("testPassword"),
                "concurrencyEscort@test.com",
                "동행인",
                Role.ESCORT,
                Gender.FEMALE,
                LocalDate.of(1995, 1, 1),
                "010-2222-2222",
                "수원"
        );

        escort = userRepository.save(escort);
        escortId = escort.getId();

        EscortProfile escortProfile =
                new EscortProfile(
                        escort,
                        "동행인 소개",
                        "테스트은행",
                        escort.getName(),
                        "1234"
                );

        escortProfile.verify(LocalDateTime.now());
        escortProfileRepository.save(escortProfile);

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

        // 두 번째 동행인 생성
        User escort2 = new User(
                "concurrencyEscort2",
                passwordEncoder.encode("testPassword"),
                "concurrencyEscort2@test.com",
                "동행인2",
                Role.ESCORT,
                Gender.MALE,
                LocalDate.of(1996, 1, 1),
                "010-3333-3333",
                "수원"
        );

        escort2 = userRepository.save(escort2);

        // 두 번째 동행인 프로필 생성
        EscortProfile escortProfile2 = new EscortProfile(
                escort2,
                "동행인2 소개",
                "테스트은행",
                escort2.getName(),
                "5678"
        );

        escortProfile2.verify(LocalDateTime.now());
        escortProfileRepository.save(escortProfile2);

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
}
