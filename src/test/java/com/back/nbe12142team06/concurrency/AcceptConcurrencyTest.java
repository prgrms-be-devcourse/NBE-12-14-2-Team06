package com.back.nbe12142team06.concurrency;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
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
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [동시성 시나리오 B] 한 공고의 두 지원을 동시에 승인.
 *
 * 이 클래스의 테스트 메서드에는 @Transactional 을 붙이지 않는다.
 * 각 스레드가 실제로 자기 트랜잭션을 커밋해야 경쟁 상태가 재현된다.
 */
@SpringBootTest
@ActiveProfiles("test")
class AcceptConcurrencyTest {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EscortProfileRepository escortProfileRepository;

    private Long postId;
    private Long clientId;
    private Long applicationId1;
    private Long applicationId2;

    @BeforeEach
    void setUp() {
        String tag = String.valueOf(System.nanoTime());
        String phoneTag = tag.substring(tag.length() - 7);

        User client = userRepository.save(User.builder()
                .username("accept-client-" + tag)
                .password("pw")
                .email("accept-client-" + tag + "@test.com")
                .name("의뢰인")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1970, 1, 1))
                .phoneNum("010" + phoneTag + "1")
                .region("수원")
                .build());

        User escort1 = userRepository.save(User.builder()
                .username("accept-escort1-" + tag)
                .password("pw")
                .email("accept-escort1-" + tag + "@test.com")
                .name("동행인1")
                .role(Role.ESCORT)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNum("010" + phoneTag + "2")
                .region("수원")
                .build());

        User escort2 = userRepository.save(User.builder()
                .username("accept-escort2-" + tag)
                .password("pw")
                .email("accept-escort2-" + tag + "@test.com")
                .name("동행인2")
                .role(Role.ESCORT)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1996, 1, 1))
                .phoneNum("010" + phoneTag + "3")
                .region("수원")
                .build());

        escortProfileRepository.save(verifiedProfile(escort1));
        escortProfileRepository.save(verifiedProfile(escort2));

        Post post = postRepository.save(Post.builder()
                .client(client)
                .title("테스트 공고")
                .content("병원 동행 테스트")
                .region("수원")
                .hospitalName("아주대학교병원")
                .hospitalAddress("경기도 수원시")
                .hospitalLat(BigDecimal.valueOf(37.2795))
                .hospitalLng(BigDecimal.valueOf(127.0476))
                .pickupAddress("경기도 수원시 팔달구")
                .pickupLat(BigDecimal.valueOf(37.2636))
                .pickupLng(BigDecimal.valueOf(127.0286))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now().plusDays(1))
                .escortStartAt(LocalDateTime.now().plusDays(2))
                .escortEndAt(LocalDateTime.now().plusDays(2).plusHours(3))
                .build());

        Application application1 = applicationRepository.save(
                Application.builder().post(post).escort(escort1).build());
        Application application2 = applicationRepository.save(
                Application.builder().post(post).escort(escort2).build());

        postId = post.getId();
        clientId = client.getId();
        applicationId1 = application1.getId();
        applicationId2 = application2.getId();
    }

    private EscortProfile verifiedProfile(User escort) {
        EscortProfile profile = new EscortProfile(escort, "자기소개", "국민은행", "동행인", "1234567890");
        profile.verify(LocalDateTime.now());
        return profile;
    }

    @Test
    @DisplayName("[동시성 B] 한 공고의 두 지원을 동시에 승인하면 한 건만 ACCEPTED 되고 나머지는 의미 있는 예외를 받아야 한다")
    void 한_공고의_두_지원을_동시에_승인() throws Exception {

        List<Long> applicationIds = List.of(applicationId1, applicationId2);
        int threadCount = applicationIds.size();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        for (Long applicationId : applicationIds) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    applicationService.accept(applicationId, clientId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failures.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        Application application1 = applicationRepository.findById(applicationId1).orElseThrow();
        Application application2 = applicationRepository.findById(applicationId2).orElseThrow();

        long acceptedCount = List.of(application1, application2).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED)
                .count();

        Post post = postRepository.findById(postId).orElseThrow();

        // ACCEPTED 는 정확히 1건이어야 한다.
        assertThat(acceptedCount).isEqualTo(1);
        assertThat(post.getPostStatus()).isEqualTo(PostStatus.MATCHED);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failures).hasSize(1);

        // accept() 는 이제 Post 를 비관적 쓰기 락(findByIdForUpdate)으로 조회한다.
        // 두 번째 요청은 첫 번째 트랜잭션이 커밋될 때까지 그 조회에서 대기했다가,
        // 커밋 후 갱신된 상태(MATCHED)를 보고 기존의 InvalidException
        // ("모집 중인 공고만 매칭할 수 있습니다")을 정상적으로 던진다.
        // 락을 잡기 전처럼 CannotAcquireLockException/DataIntegrityViolationException 같은
        // 원시 DataAccessException 이 새어나오지 않아야 한다.
        assertThat(failures.get(0)).isNotInstanceOf(DataAccessException.class);
    }
}
