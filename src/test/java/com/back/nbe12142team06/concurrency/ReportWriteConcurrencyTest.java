package com.back.nbe12142team06.concurrency;

import com.back.nbe12142team06.DatabaseCleaner;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.report.dto.ReportWriteRequest;
import com.back.nbe12142team06.domain.report.entity.Department;
import com.back.nbe12142team06.domain.report.repository.ReportRepository;
import com.back.nbe12142team06.domain.report.service.ReportService;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [동시성 시나리오 D] 같은 동행 건에 보고서가 동시에 작성됨.
 *
 * 이 클래스의 테스트 메서드에는 @Transactional 을 붙이지 않는다.
 * 각 스레드가 실제로 자기 트랜잭션을 커밋해야 경쟁 상태가 재현된다.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReportWriteConcurrencyTest {

    @Autowired
    private ReportService reportService;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private Long escortId;
    private Long applicationId;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        String tag = String.valueOf(System.nanoTime());
        String phoneTag = tag.substring(tag.length() - 7);

        User client = userRepository.save(User.builder()
                .username("report-client-" + tag)
                .password("pw")
                .email("report-client-" + tag + "@test.com")
                .name("의뢰인")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(java.time.LocalDate.of(1970, 1, 1))
                .phoneNum("010" + phoneTag + "1")
                .region("수원")
                .build());

        User escort = userRepository.save(User.builder()
                .username("report-escort-" + tag)
                .password("pw")
                .email("report-escort-" + tag + "@test.com")
                .name("동행인")
                .role(Role.ESCORT)
                .gender(Gender.FEMALE)
                .birthDate(java.time.LocalDate.of(1995, 1, 1))
                .phoneNum("010" + phoneTag + "2")
                .region("수원")
                .build());

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

        Application application = applicationRepository.save(
                Application.builder().post(post).escort(escort).build());

        escortId = escort.getId();
        applicationId = application.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("[동시성 D] 같은 동행 건에 보고서를 동시에 작성하면 1건만 생기고 나머지는 의미 있는 예외를 받아야 한다")
    void 같은_동행_건에_보고서_동시_작성() throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        CopyOnWriteArrayList<Throwable> failures = new CopyOnWriteArrayList<>();

        for (int i = 0; i < 2; i++) {
            int idx = i;
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    ReportWriteRequest request = new ReportWriteRequest(
                            Department.ETC,
                            "목적" + idx,
                            "내용" + idx,
                            null
                    );
                    reportService.write(applicationId, escortId, request);
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

        long reportCount = reportRepository.findByApplicationId(applicationId).isPresent() ? 1 : 0;

        assertThat(reportCount).isEqualTo(1);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failures).hasSize(1);

        // ReportService.write() 의 existsByApplicationId() 검사는 여전히 check-then-act 라
        // 두 스레드가 동시에 통과할 수 있지만, 두 번째 save() 가 Report.application_id
        // unique 제약에 걸려 던지는 DataIntegrityViolationException 을 서비스 계층에서
        // 잡아 "이미 보고서가 작성됨" 을 뜻하는 DuplicatedException 으로 바꿔 던지므로,
        // 실패한 쪽도 원시 예외가 아닌 의미 있는 비즈니스 예외를 받는다.
        assertThat(failures.get(0)).isNotInstanceOf(DataIntegrityViolationException.class);
    }
}
