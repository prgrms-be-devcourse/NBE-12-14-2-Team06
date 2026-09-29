package com.back.nbe12142team06.concurrency;

import com.back.nbe12142team06.DatabaseCleaner;
import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [회귀] 지원 -> 취소 -> 재지원 -> 재취소 흐름 검증.
 *
 * ApplyConcurrencyTest(시나리오 A)의 중복 지원 방지를 (post_id, escort_id, status)
 * 복합 unique 제약으로 구현했을 때, 이 흐름이 두 번째 취소에서 막혔다.
 * 취소된 지원 행이 두 건 이상 쌓이면 (post_id, escort_id, CANCELED) 조합끼리
 * 서로 충돌해 DataIntegrityViolationException 이 났기 때문이다.
 * Application.activePostId (진행 중인 지원만 가리키는 컬럼)로 바꿔 해결했고,
 * 이 테스트는 그 회귀를 다시 잡아내기 위한 것이다.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplyCancelReapplyTest {

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
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private Long postId;
    private Long escortId;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
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
        escortProfileRepository.save(escortProfile);

        transactionTemplate.executeWithoutResult(status ->
                escortProfileRepository.verify(escort.getId(), LocalDateTime.now()));

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

        postId = post.getId();
        escortId = escort.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }


    @Test
    @DisplayName("[회귀] 지원 → 취소 → 재지원 → 재취소 가 모두 가능해야 한다")
    void 지원_취소_재지원_재취소가_모두_가능해야_한다() {
        Long first = applicationService.apply(postId, escortId).id();
        applicationService.cancel(first, escortId);

        Long second = applicationService.apply(postId, escortId).id();
        applicationService.cancel(second, escortId);   // 복합 unique 제약이었을 때 여기서 터졌다.

        List<Application> all = applicationRepository.findAll().stream()
                .filter(a -> a.getPost().getId().equals(postId) && a.getEscort().getId().equals(escortId))
                .toList();
        assertThat(all).hasSize(2);
    }
}
