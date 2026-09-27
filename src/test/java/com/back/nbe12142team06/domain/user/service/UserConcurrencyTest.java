package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    @DisplayName("같은 username으로 동시에 가입하면 1명만 가입되고 나머지는 DuplicatedException으로 거절된다")
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
}