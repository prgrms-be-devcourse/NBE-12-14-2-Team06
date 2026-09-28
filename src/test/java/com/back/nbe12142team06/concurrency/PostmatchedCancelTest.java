package com.back.nbe12142team06.concurrency;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.penalty.repository.NoShowPenaltyRepository;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PostmatchedCancelTest {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private PostService postService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EscortProfileRepository escortProfileRepository;
    @Autowired
    private NoShowPenaltyRepository noShowPenaltyRepository;

    private Long postId;
    private Long escortId;
    private Long clientId;
    private Long applicationId;

    @BeforeEach
    void setUp() {
        String tag = String.valueOf(System.nanoTime());
        String phoneTag = tag.substring(tag.length() - 7);

        User client = userRepository.save(User.builder()
                .username("apply-client-" + tag)
                .password("pw")
                .email("apply-client-" + tag + "@test.com")
                .name("의뢰인")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1970, 1, 1))
                .phoneNum("010" + phoneTag + "1")
                .region("수원")
                .build());

        User escort = userRepository.save(User.builder()
                .username("apply-escort-" + tag)
                .password("pw")
                .email("apply-escort-" + tag + "@test.com")
                .name("동행인")
                .role(Role.ESCORT)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNum("010" + phoneTag + "2")
                .region("수원")
                .build());

        EscortProfile escortProfile = new EscortProfile(escort, "자기소개", "국민은행", "동행인", "1234567890");
        escortProfile.verify(LocalDateTime.now());
        escortProfileRepository.save(escortProfile);

        //@BeforeEach에서 Post:MATCHED 상태 + Application:ACCEPTED 상태 하나 미리 세팅
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

        // Post: OPEN -> MATCHED (엔티티 메서드로 전이 후 재저장 — 이 테스트 클래스엔
        // @Transactional 이 없어서 더티체킹이 안 걸리므로 save()를 다시 호출해야 반영된다)
        post.match();
        post = postRepository.save(post);

        // Application: PENDING -> ACCEPTED
        Application application = Application.builder()
                .post(post)
                .escort(escort)
                .build();
        application.accept();
        application = applicationRepository.save(application);

        postId = post.getId();
        escortId = escort.getId();
        clientId = client.getId();
        applicationId = application.getId();
    }

    // 이 테스트는 노쇼취소 경로를 타면서 NoShowPenalty 행을 만든다. 이 클래스는
    // (다른 동시성 테스트들과 달리) applicationRepository.deleteAll() 을 하지
    // 않기 때문에 그 자체로는 문제가 없지만, 같은 Spring 컨텍스트/H2 DB를
    // 공유하는 ApplicationConcurrencyTest 의 @BeforeEach 가 전체 application
    // 테이블을 deleteAll() 하다가, 여기서 남긴 NoShowPenalty 가 그 application
    // 행을 참조하고 있어서 FK 제약 위반으로 실패한다. 그래서 이 테스트가 만든
    // 행은 반드시 직접 치워야 한다.
    @AfterEach
    void tearDown() {
        noShowPenaltyRepository.deleteAll(noShowPenaltyRepository.findByEscortIdAndStatus(escortId));
        applicationRepository.deleteById(applicationId);
        postRepository.deleteById(postId);
    }

    /**
     * [동시성 B] 매칭된 공고를 의뢰인이 매칭취소하는 순간, 동행인이 자기 지원을
     * 노쇼취소(ACCEPTED -> noShow)하면 어떻게 되는지 확인한다.
     *
     * PostService.matchedCancel() / ApplicationService.cancel() 은 둘 다
     * findById() 로 락 없이 Post/Application 을 읽고 check-then-act 로 상태를
     * 바꾼다. apply()/accept() 와 달리 findByIdWithLock() 을 안 쓰기 때문에,
     * 두 트랜잭션이 서로 다른 스냅샷을 보고 각자 커밋하게 되고, 결과적으로
     * "나중에 커밋하는 쪽" 이 Post 의 최종 상태를 덮어쓰게 된다.
     */
    @Test
    @DisplayName("[동시성 B] 매칭취소와 노쇼취소가 동시에 들어오면 Post 상태가 꼬이면 안 된다")
    void 매칭취소와_노쇼취소_동시_요청() throws InterruptedException {

        // 3. 스레드 2개 + 3종 래치
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // 4. 스레드 1 - 의뢰인의 매칭취소
        executorService.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                postService.matchedCancel(postId, clientId);
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName()
                        + " 매칭취소 실패: " + e.getClass().getSimpleName() + " / " + e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        });

        // 4. 스레드 2 - 동행인의 노쇼취소 (ACCEPTED -> noShow, Post.reopen() 호출)
        executorService.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                applicationService.cancel(applicationId, escortId);
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName()
                        + " 노쇼취소 실패: " + e.getClass().getSimpleName() + " / " + e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // 5. 최종 DB 상태로 검증
        Post resultPost = postRepository.findById(postId).orElseThrow();
        Application resultApplication = applicationRepository.findById(applicationId).orElseThrow();

        System.out.println("최종 공고 상태 = " + resultPost.getPostStatus());
        System.out.println("최종 지원 상태 = " + resultApplication.getStatus());

        /*
         * 두 경로 모두 noShow() 처리 자체는 항상 반영된다(Application 은 서로
         * 다른 트랜잭션에서 각자 조회하지만 충돌하는 다른 스레드가 없으므로).
         * 갈리는 건 Post 상태다 - 누가 나중에 커밋하느냐에 따라 둘 중 하나다.
         *
         * 1. 매칭취소가 나중에 반영됨 -> Post = CANCELED
         * 2. 노쇼취소(재오픈)가 나중에 반영됨 -> Post = OPEN
         *
         * 락이 없으니 어느 쪽이 이길지는 매 실행마다 달라질 수 있다. 만약 이 중
         * 어느 조합에도 안 걸리면(예: Post 가 여전히 MATCHED 로 남아 둘 다 반영이
         * 안 됨) 그게 바로 findByIdWithLock() 을 이 경로에도 추가해야 한다는 증거
         * =>postservice,applicationservice에 findByIdWithLock() 추가완료
         */
        boolean validState =
                (resultApplication.getStatus() == ApplicationStatus.NO_SHOW
                        && resultPost.getPostStatus() == PostStatus.CANCELED)
                        || (resultApplication.getStatus() == ApplicationStatus.NO_SHOW
                        && resultPost.getPostStatus() == PostStatus.OPEN);

        assertTrue(validState, "공고 상태와 지원 상태 조합이 예상 범위를 벗어났습니다. post="
                + resultPost.getPostStatus() + ", application=" + resultApplication.getStatus());
    }
}
