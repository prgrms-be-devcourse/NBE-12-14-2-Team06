package com.back.nbe12142team06.domain.user.service;


import com.back.nbe12142team06.DatabaseCleaner;
import com.back.nbe12142team06.domain.user.dto.profile.EscortProfileModifyRequest;
import com.back.nbe12142team06.domain.user.dto.profile.EscortProfileRequest;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class EscortProfileConcurrencyTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EscortProfileRepository escortProfileRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    // username만 같고 email, phoneNum은 스레드마다 다르게
    private UserSignUpRequest createRequest(String username, int i) {
        return new UserSignUpRequest(
                username,
                "Password123!",
                "user" + i + "@test.com",
                "테스트",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                "010" + String.format("%08d", i),   // 01000000000 ~ 01000000009
                "서울"
        );
    }

    // TODO: 교육 이수와 프로필 수정이 겹칠 때
    @Test
    @DisplayName("[EscortProfileConcurrencyTest] 교육 이수와 프로필 수정이 겹칠 때")
    void t1() throws InterruptedException {
        Long userId = this.userService.signUp(createRequest("user1", 0)).getId();

        EscortProfileRequest createEscortProfile = new EscortProfileRequest(
                "배 아파요. 너무 졸려요.",
                "귀가 은행",
                "최동행",
                "1111-1111-1111"
        );

        EscortProfileModifyRequest updateEscortProfile = new EscortProfileModifyRequest(
                "가나다라마바사",
                "한글 은행",
                "박동행",
                "1111-1122-1111"
        );

        this.userService.createEscortProfile(userId, createEscortProfile);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        AtomicBoolean verifySucceeded = new AtomicBoolean();
        AtomicBoolean modifySucceeded = new AtomicBoolean();
        List<Throwable> unexpected = Collections.synchronizedList(new ArrayList<>());

        boolean finished;

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            // 교육 이수
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    transactionTemplate.executeWithoutResult(status -> {
                        EscortProfile profile = this.escortProfileRepository.findById(userId).orElseThrow();
                        this.escortProfileRepository.verify(profile.getUserId(), LocalDateTime.now());
                    }); // 여기서 커밋 → @Version 비교
                    verifySucceeded.set(true);
                } catch (OptimisticLockingFailureException e) {
                    // 충돌 시 한쪽 실패는 정상
                } catch (Throwable e) {
                    unexpected.add(e);
                } finally {
                    done.countDown();
                }
            });

            // 프로필 수정
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    this.userService.updateEscortProfile(userId, updateEscortProfile);
                    modifySucceeded.set(true);
                } catch (OptimisticLockingFailureException e) {
                    // 충돌 시 한쪽 실패는 정상
                } catch (Throwable e) {
                    unexpected.add(e);
                } finally {
                    done.countDown();
                }
            });

            // 시작
            ready.await();
            start.countDown();
            finished = done.await(10, TimeUnit.SECONDS);
        }

        EscortProfile result = this.escortProfileRepository.findById(userId)
                .orElseThrow();

        assertThat(finished).as("제한 시간 내 종료").isTrue();
        assertThat(unexpected).as("예상하지 못한 예외").isEmpty();
        assertThat(verifySucceeded.get() || modifySucceeded.get()).as("최소 한쪽은 성공").isTrue();

        if (verifySucceeded.get()) {
            assertThat(result.getVerified()).as("신원 인증 유지").isTrue();
        }
        if (modifySucceeded.get()) {
            assertThat(result.getBankName()).as("수정 내용 유지").isEqualTo("한글 은행");
            assertThat(result.getAccountNumber()).isEqualTo("1111-1122-1111");
        }
    }





    // TODO: 계좌 변경과 리뷰 평점 반영이 겹칠 때
    // TODO: 리뷰 두 건이 동시에 들어올 때
    // TODO: 리뷰 여러 건이 한꺼번에 몰릴 때
    // TODO: 동행 완료와 노쇼가 겹칠 때


}
