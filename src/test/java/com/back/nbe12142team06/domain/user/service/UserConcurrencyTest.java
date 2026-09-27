package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.dto.user.UserProfileUpdateRequest;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserConcurrencyTest {

    private static final int THREAD_COUNT = 10;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        this.userRepository.deleteAllInBatch();
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

    @Test
    @DisplayName("[UserService] 회원가입 - 같은 username으로 동시에 가입하면 1명만 가입되고 나머지는 DuplicatedException으로 거절")
    void t1() throws InterruptedException {
        // given
        String username = "concurrent_user";

        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREAD_COUNT);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger duplicated = new AtomicInteger();
        List<Throwable> unexpected = Collections.synchronizedList(new ArrayList<>());

        // when
        boolean finished;
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                UserSignUpRequest request = createRequest(username, i);
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        userService.signUp(request);
                        success.incrementAndGet();
                    } catch (DuplicatedException e) {
                        // email, phoneNum은 스레드마다 다르므로 중복 예외는 username 중복뿐
                        duplicated.incrementAndGet();
                    } catch (Throwable e) {
                        unexpected.add(e);
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await();
            start.countDown();
            finished = done.await(10, TimeUnit.SECONDS);
        }

        assertThat(finished).isTrue();
        assertThat(unexpected).as("예상하지 못한 예외", unexpected).isEmpty();  // 10개 중 0개의 예상치 못한 예외
        assertThat(success.get()).isEqualTo(1);     // 전체 요청 중 1개 성공
        assertThat(duplicated.get()).isEqualTo(THREAD_COUNT - 1);   // 전체 요청 중 1개를 제외히고는 전부 중복 예외
        assertThat(userRepository.count()).isEqualTo(1);    // 실제 저장된 데이터는 1개의 행 뿐
    }

    @Test
    @DisplayName("[UserService] 탈퇴와 수정 - 탈퇴 요청과 수정 요청이 동시에 실행돼도 탈퇴 시 마스킹 유지")
    void t2() throws InterruptedException {
        Long userId = this.userService.signUp(createRequest("user1", 0)).getId();

        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest(
                "NewPassword123!",
                "new@test.com",
                "새이름",
                LocalDate.of(1991, 1, 1),
                "01099999999",
                "부산"
        );


        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        AtomicBoolean withdrawSucceeded = new AtomicBoolean(false);
        List<Throwable> unexpected = Collections.synchronizedList(new ArrayList<>());

        boolean finished;
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            // 탈퇴
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    this.userService.deleteMyProfile(userId);
                    withdrawSucceeded.set(true);
                } catch (OptimisticLockingFailureException e) {
                    // 수정이 먼저 커밋되어 충돌로 거절은 허용
                } catch (Throwable e) {
                    unexpected.add(e);
                } finally {
                    done.countDown();
                }
            });

            // 회원 수정
            executor.submit(() -> {
               ready.countDown();
               try {
                   start.await();
                   this.userService.updateMyProfile(userId, updateRequest);
               } catch (OptimisticLockingFailureException | UnauthorizedException e) {
                   // 탈퇴가 먼저 커밋되어 충돌로 거절, 이미 탈퇴된 뒤 조회는 허용
               } catch (Throwable e) {
                   unexpected.add(e);
               } finally {
                   done.countDown();
               }
            });

            ready.await();
            start.countDown();
            finished = done.await(10, TimeUnit.SECONDS);
        }

        assertThat(finished).as("제한 시간 내 종료").isTrue();

        assertThat(unexpected).as("예상하지 못한 예외", unexpected).isEmpty();

        User result = this.userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow();
        String masked = "deleted_" + userId;

        if (withdrawSucceeded.get()) {
            assertThat(result.isDeleted()).as("탈퇴 상태 유지").isTrue();
            assertThat(result.getUsername()).as("username 마스킹 유지").isEqualTo(masked);
            assertThat(result.getEmail()).as("email 마스킹 유지").isEqualTo(masked);
            assertThat(result.getPhoneNum()).as("phoneNum 마스킹 유지").isEqualTo(masked);
        } else {
            assertThat(result.isDeleted()).as("탈퇴 실패 시 회원 유지").isFalse();
            assertThat(result.getEmail()).as("수정 반영").isEqualTo("new@test.com");
        }
    }
}