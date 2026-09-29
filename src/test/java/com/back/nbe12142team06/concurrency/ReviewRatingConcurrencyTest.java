package com.back.nbe12142team06.concurrency;

import com.back.nbe12142team06.DatabaseCleaner;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.review.dto.ReviewWriteRequest;
import com.back.nbe12142team06.domain.review.service.ReviewService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [동시성 시나리오 C] 한 동행인의 서로 다른 완료된 동행 건에 리뷰 2건이 동시에 작성됨.
 *
 * 이 클래스의 테스트 메서드에는 @Transactional 을 붙이지 않는다.
 * 각 스레드가 실제로 자기 트랜잭션을 커밋해야 경쟁 상태가 재현된다.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReviewRatingConcurrencyTest {

    @Autowired
    private ReviewService reviewService;
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

    private Long escortId;
    private Long clientId1;
    private Long clientId2;
    private Long applicationId1;
    private Long applicationId2;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        String tag = String.valueOf(System.nanoTime());
        String phoneTag = tag.substring(tag.length() - 7);

        User escort = userRepository.save(User.builder()
                .username("review-escort-" + tag)
                .password("pw")
                .email("review-escort-" + tag + "@test.com")
                .name("동행인")
                .role(Role.ESCORT)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNum("010" + phoneTag + "1")
                .region("수원")
                .build());

        User client1 = userRepository.save(User.builder()
                .username("review-client1-" + tag)
                .password("pw")
                .email("review-client1-" + tag + "@test.com")
                .name("의뢰인1")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1950, 1, 1))
                .phoneNum("010" + phoneTag + "2")
                .region("수원")
                .build());

        User client2 = userRepository.save(User.builder()
                .username("review-client2-" + tag)
                .password("pw")
                .email("review-client2-" + tag + "@test.com")
                .name("의뢰인2")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1955, 1, 1))
                .phoneNum("010" + phoneTag + "3")
                .region("수원")
                .build());

        escortProfileRepository.save(new EscortProfile(escort, "자기소개", "국민은행", "동행인", "1234567890"));

        Post post1 = postRepository.save(completedPost(client1));
        Post post2 = postRepository.save(completedPost(client2));

        Application application1 = applicationRepository.save(
                Application.builder().post(post1).escort(escort).status(ApplicationStatus.ACCEPTED).build());
        Application application2 = applicationRepository.save(
                Application.builder().post(post2).escort(escort).status(ApplicationStatus.ACCEPTED).build());

        escortId = escort.getId();
        clientId1 = client1.getId();
        clientId2 = client2.getId();
        applicationId1 = application1.getId();
        applicationId2 = application2.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    private Post completedPost(User client) {
        return Post.builder()
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
                .recruitStartAt(LocalDateTime.now().minusDays(3))
                .recruitEndAt(LocalDateTime.now().minusDays(2))
                .escortStartAt(LocalDateTime.now().minusDays(1))
                .escortEndAt(LocalDateTime.now().minusDays(1).plusHours(3))
                .postStatus(PostStatus.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("[동시성 C] 같은 동행인에게 리뷰 2건이 동시에 작성되면 평점 합계와 개수가 모두 반영되어야 한다")
    void 같은_동행인에게_리뷰_2건이_동시에_작성() throws Exception {

        int rating1 = 4;
        int rating2 = 5;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        CopyOnWriteArrayList<Throwable> failures = new CopyOnWriteArrayList<>();

        executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                reviewService.write(applicationId1, clientId1, new ReviewWriteRequest(rating1, null, null));
                successCount.incrementAndGet();
            } catch (Exception e) {
                failures.add(e);
            } finally {
                doneLatch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                reviewService.write(applicationId2, clientId2, new ReviewWriteRequest(rating2, null, null));
                successCount.incrementAndGet();
            } catch (Exception e) {
                failures.add(e);
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(failures).isEmpty();
        assertThat(successCount.get()).isEqualTo(2);

        EscortProfile escortProfile = escortProfileRepository.findById(escortId).orElseThrow();

        // EscortProfileRepository.addRating() 은 엔티티를 읽어 절대값으로 덮어쓰는 대신
        // DB 에서 "현재 값 + 증가분" 을 원자적으로 계산하는 벌크 UPDATE 라, 두 트랜잭션이
        // 동시에 커밋해도 lost update 없이 두 별점이 모두 반영된다.
        assertThat(escortProfile.getRatingCount()).isEqualTo(2);
        assertThat(escortProfile.getRatingSum()).isEqualTo(rating1 + rating2);
    }
}
