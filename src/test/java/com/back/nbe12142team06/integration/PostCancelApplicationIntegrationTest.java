package com.back.nbe12142team06.integration;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.application.service.ApplicationService;
import com.back.nbe12142team06.domain.payment.client.TossPaymentClient;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.ride.entity.RideSelect;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 공고 취소 / 매칭 취소 시 그 공고의 지원이 함께 정리되는지 확인하는 통합 테스트.
 * 테스트마다 트랜잭션이 롤백되어 데이터가 남지 않는다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostCancelApplicationIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private EscortProfileRepository escortProfileRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    // 외부 결제 API 는 호출하지 않는다
    @MockitoBean
    private TossPaymentClient tossPaymentClient;

    private User client;
    private User escortA;
    private User escortB;

    @BeforeEach
    void setUp() {
        client = signUp("cancelClient", "의뢰인", Role.CLIENT, "010-5552-0001");
        escortA = signUpEscort("cancelEscortA", "동행인A", "010-5552-0002");
        escortB = signUpEscort("cancelEscortB", "동행인B", "010-5552-0003");
    }

    private User signUp(String username, String name, Role role, String phone) {
        return userService.signUp(new UserSignUpRequest(
                username, "testPassword1!", username + "@test.test", name, role,
                Gender.MALE, LocalDate.of(1990, 5, 20), phone, "서울시"));
    }

    // 동행 매니저는 프로필을 만들고 교육 이수(verify)까지 해야 지원할 수 있다.
    private User signUpEscort(String username, String name, String phone) {
        User escort = signUp(username, name, Role.ESCORT, phone);
        escortProfileRepository.save(new EscortProfile(escort, "소개", "은행", name, "000-1234567-000"));
        escortProfileRepository.verify(escort.getId(), LocalDateTime.now());
        return escort;
    }

    // 공고를 작성하고 결제 완료 상태로 만든다. daysLater: 동행 날짜 (같은 값이면 시간이 겹친다)
    private Post newPost(int daysLater) {
        PostWriteRequest request = new PostWriteRequest(
                "정형외과 동행 구합니다", "무릎 수술 후 검진 예약이 있어 동행인이 필요합니다.", "서울",
                "서울성모병원", "서울 서초구 반포대로 222",
                BigDecimal.valueOf(37.5012743), BigDecimal.valueOf(127.0051893),
                "서울 서초구 잠원동 10-1", BigDecimal.valueOf(37.5160000), BigDecimal.valueOf(127.0200000),
                15_000,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(daysLater - 1),
                LocalDateTime.now().plusDays(daysLater), LocalDateTime.now().plusDays(daysLater).plusHours(4),
                RideSelect.TAXI, RideSelect.TAXI, "", true);

        Post post = postService.findById(postService.write(client.getId(), request).id());
        paymentRepository.testStatusDone(post.getId());
        return post;
    }

    private Long apply(Post post, User escort) {
        return applicationService.apply(post.getId(), escort.getId()).id();
    }

    private ApplicationStatus statusOf(Long applicationId) {
        return applicationRepository.findById(applicationId).orElseThrow().getStatus();
    }

    @Test
    @DisplayName("[통합] 모집 중인 공고를 취소하면 그 공고의 대기 지원은 모두 취소되고, 다른 공고의 지원은 그대로다")
    void 공고를_취소하면_대기_지원이_취소된다() {
        Post post = newPost(7);
        Post otherPost = newPost(20);
        Long applicationA = apply(post, escortA);
        Long applicationB = apply(post, escortB);
        Long otherApplication = apply(otherPost, escortA);   // 다른 공고에 한 지원

        postService.cancel(post.getId(), client.getId());

        assertThat(postService.findById(post.getId()).getPostStatus()).isEqualTo(PostStatus.CANCELED);
        assertThat(statusOf(applicationA)).isEqualTo(ApplicationStatus.CANCELED);
        assertThat(statusOf(applicationB)).isEqualTo(ApplicationStatus.CANCELED);
        assertThat(statusOf(otherApplication)).isEqualTo(ApplicationStatus.PENDING);   // 다른 공고는 영향 없음
    }

    @Test
    @DisplayName("[통합] 매칭 취소하면 승인된 지원이 패널티 없이 취소되고, 그 동행인이 겹치는 시간의 다른 공고에 다시 매칭될 수 있다")
    void 매칭을_취소하면_승인_지원이_패널티없이_취소된다() {
        Post post = newPost(7);
        Long application = apply(post, escortA);
        applicationService.accept(application, client.getId());   // 공고가 MATCHED, 지원은 ACCEPTED

        postService.matchedCancel(post.getId(), client.getId());

        assertThat(statusOf(application)).isEqualTo(ApplicationStatus.CANCELED);
        // 패널티 없음: 노쇼 횟수가 늘지 않는다
        assertThat(escortProfileRepository.findById(escortA.getId()).orElseThrow().getNoShowCount()).isZero();

        // 취소된 지원이 승인 상태로 남아 있으면 같은 시간대의 다른 공고 승인이 "시간 겹침"으로 막힌다.
        Post sameTimePost = newPost(7);
        Long newApplication = apply(sameTimePost, escortA);
        applicationService.accept(newApplication, client.getId());   // 예외 없이 승인되어야 한다
        assertThat(statusOf(newApplication)).isEqualTo(ApplicationStatus.ACCEPTED);
    }
}
