package com.back.nbe12142team06.domain.penalty.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.penalty.entity.NoShowPenalty;
import com.back.nbe12142team06.domain.penalty.entity.NoShowPenaltyStatus;
import com.back.nbe12142team06.domain.penalty.repository.NoShowPenaltyRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoShowPenaltyServiceTest {

    // 시급
    private final int HOURLY_PAY = 15_000;
    // 동행 시간
    private final int HOURS = 10;
    // 의뢰인 결제 금액
    private final int PAYOUT_AMOUNT = HOURLY_PAY * HOURS;
    // 플랫폼 수수료
    private final int PLATFORM_FEE = (int) (PAYOUT_AMOUNT * 0.1);
    // 패널티 차감 전 정산 금액
    private final int BASE_AMOUNT = PAYOUT_AMOUNT - PLATFORM_FEE;
    // 패널티 금액
    private final int PENALTY_AMOUNT = (int) (BASE_AMOUNT * 0.1);

    @Autowired
    private EntityManager em;
    @Autowired
    private NoShowPenaltyService noShowPenaltyService;
    @Autowired
    private NoShowPenaltyRepository noShowPenaltyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private SettlementRepository settlementRepository;

    private Post createPost(User client, Long id, PostStatus status) {
        String title = "정형외과 동행 구합니다";
        String content = "무릎 수술 후 검진 예약이 있어 동행인이 필요합니다.";
        String postRegion = "서울";
        String hospitalName = "서울성모병원";
        String hospitalAddress = "서울 서초구 반포대로 222";
        BigDecimal hospitalLat = BigDecimal.valueOf(37.5012743);
        BigDecimal hospitalLng = BigDecimal.valueOf(127.0051893);
        String pickupAddress = "서울 서초구 잠원동 10-1";
        BigDecimal pickupLat = BigDecimal.valueOf(37.5160000);
        BigDecimal pickupLng = BigDecimal.valueOf(127.0200000);
        LocalDateTime recruitStartAt = LocalDateTime.now().plusDays(1);
        LocalDateTime recruitEndAt = LocalDateTime.now().plusDays(6);
        LocalDateTime escortStartAt = LocalDateTime.now().plusDays(7);
        LocalDateTime escortEndAt = LocalDateTime.now().plusDays(7).plusHours(HOURS);

        return Post.builder()
                .id(id)
                .title(title)
                .content(content)
                .region(postRegion)
                .hospitalName(hospitalName)
                .hospitalAddress(hospitalAddress)
                .hospitalLat(hospitalLat)
                .hospitalLng(hospitalLng)
                .pickupAddress(pickupAddress)
                .pickupLat(pickupLat)
                .pickupLng(pickupLng)
                .hourlyPay(HOURLY_PAY)
                .recruitStartAt(recruitStartAt)
                .recruitEndAt(recruitEndAt)
                .escortStartAt(escortStartAt)
                .escortEndAt(escortEndAt)
                .postStatus(status)
                .client(client)
                .build();
    }

    private User createClient() {
        return User.builder()
                .username("client01")
                .password("password1!")
                .email("client01@email.com")
                .name("의뢰인")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1960, 11, 11))
                .phoneNum("010-1234-1234")
                .region("서울")
                .build();
    }

    private User createEscort() {
        return User.builder()
                .username("escort01")
                .password("password1!")
                .email("escort01@email.com")
                .name("동행매니저")
                .role(Role.ESCORT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNum("010-5678-5678")
                .region("서울")
                .build();
    }

    private Application createApplication(User escort, Post post, ApplicationStatus status) {
        return Application.builder()
                .post(post)
                .escort(escort)
                .status(status)
                .acceptedPostId(post.getId())
                .activePostId(post.getId())
                .build();
    }

    private Settlement createSettlement(Application application, User escort) {
        return Settlement.builder()
                .payoutAmount(BASE_AMOUNT - PENALTY_AMOUNT)
                .platformFee(PLATFORM_FEE)
                .penaltyAmount(PENALTY_AMOUNT)
                .settlementStatus(SettlementStatus.PENDING)
                .settledDate(LocalDate.now().plusDays(1))
                .application(application)
                .escort(escort)
                .build();
    }

    // 노쇼가 발생한 지원 하나를 DB 에 올려둔다. (의뢰인 - 공고 - 동행 매니저 - 지원)
    private Application setUpNoShowApplication() {
        User client = createClient();
        User escort = createEscort();
        Post post = createPost(client, null, PostStatus.COMPLETED);
        Application application = createApplication(escort, post, ApplicationStatus.NO_SHOW);

        userRepository.save(client);
        userRepository.save(escort);
        postRepository.save(post);

        return applicationRepository.save(application);
    }

    @Test
    @DisplayName("[NoShowPenaltyService] APPLIED 패널티는 재적용 대상에서 제외된다")
    void appliedPenaltyNotReapplied() {
        Application application = setUpNoShowApplication();
        Long escortId = application.getEscort().getId();

        noShowPenaltyService.noShow(application);
        em.flush();

        // 생성 직후에는 PENDING 이라 적용 대상으로 조회된다
        NoShowPenalty pendingPenalty = noShowPenaltyService.getNoShowPenalty(escortId).orElseThrow();
        assertThat(pendingPenalty.getStatus()).isEqualTo(NoShowPenaltyStatus.PENDING);

        Settlement settlement = settlementRepository.save(createSettlement(application, application.getEscort()));
        noShowPenaltyService.applyPenalty(escortId, PENALTY_AMOUNT, BASE_AMOUNT, settlement, pendingPenalty);

        em.flush();
        em.clear();

        // 적용 결과가 패널티에 남는다
        NoShowPenalty appliedPenalty = noShowPenaltyRepository.findById(pendingPenalty.getId()).orElseThrow();
        assertThat(appliedPenalty.getStatus()).isEqualTo(NoShowPenaltyStatus.APPLIED);
        assertThat(appliedPenalty.getAmount()).isEqualTo(PENALTY_AMOUNT);
        assertThat(appliedPenalty.getBaseAmount()).isEqualTo(BASE_AMOUNT);
        assertThat(appliedPenalty.getSettlement().getId()).isEqualTo(settlement.getId());

        // 이후 정산에서는 다시 적용 대상으로 잡히지 않는다 (findByEscortIdAndStatus 가 PENDING 만 조회)
        assertThat(noShowPenaltyService.getNoShowPenalty(escortId)).isEmpty();
    }

    @Test
    @DisplayName("[NoShowPenaltyService] 같은 지원에 패널티 2번 생성 방지")
    void penaltyUniqueConstraint() {
        Application application = setUpNoShowApplication();

        noShowPenaltyService.noShow(application);
        em.flush();

        // application_id 에 unique 가 걸려 있어 같은 지원으로는 두 번째 패널티를 만들 수 없다
        assertThatThrownBy(() -> {
            noShowPenaltyService.noShow(application);
            em.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        em.clear();
        assertThat(noShowPenaltyRepository.findAll()).hasSize(1);
    }
}
