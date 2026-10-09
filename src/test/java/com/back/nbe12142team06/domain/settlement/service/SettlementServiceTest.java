package com.back.nbe12142team06.domain.settlement.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.settlement.client.SettlementClient;
import com.back.nbe12142team06.domain.settlement.client.SettlementClientResponse;
import com.back.nbe12142team06.domain.settlement.dto.AccountDto;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import com.back.nbe12142team06.domain.settlement.repository.SettlementRepository;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.EscortProfileRepository;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.InternalServerErrorException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SettlementServiceTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private SettlementService settlementService;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private EscortProfileRepository escortProfileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @MockitoSpyBean
    private SettlementPersistenceService settlementPersistenceService;
    @MockitoBean
    private SettlementClient settlementClient;

    private Payment createPayment(Post post, PaymentStatus status, Long id) {
        int hourlyPaySnapshot = 15_000;
        BigDecimal hours = BigDecimal.TEN;
        int amount = hourlyPaySnapshot * hours.intValue();
        Payment payment = Payment.builder()
                .id(id)
                .post(post)
                .amount(amount)
                .hourlyPaySnapshot(hourlyPaySnapshot)
                .hours(hours)
                .orderId(UUID.randomUUID().toString())
                .paymentKey(UUID.randomUUID().toString())
                .method("CARD")
                .paymentStatus(status)
                .approvedAt(LocalDateTime.now().minusDays(2))
                .balanceAmount(amount)
                .build();
        return payment;
    }

    private Payment createPayment(Post post, PaymentStatus status) {
        return createPayment(post, status, 1L);
    }

    private Post createPost(User client, Long id, int hour, PostStatus status) {
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
        int hourlyPay = 15_000;
        LocalDateTime recruitStartAt = LocalDateTime.now().plusDays(1);
        LocalDateTime recruitEndAt = LocalDateTime.now().plusDays(6);
        LocalDateTime escortStartAt = LocalDateTime.now().plusDays(7);
        LocalDateTime escortEndAt = LocalDateTime.now().plusDays(7).plusHours(hour);

        Post post = Post.builder()
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
                .hourlyPay(hourlyPay)
                .recruitStartAt(recruitStartAt)
                .recruitEndAt(recruitEndAt)
                .escortStartAt(escortStartAt)
                .escortEndAt(escortEndAt)
                .postStatus(status)
                .client(client)
                .build();

        return post;
    }

    private Post createPost(User client, Long id, PostStatus status) {
        return createPost(client, id, 10, status);
    }

    private User createClient(boolean addId) {
        User client = User.builder()
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
        if (addId) {
            try {
                Field clientIdField = client.getClass().getDeclaredField("id");
                clientIdField.setAccessible(true);
                clientIdField.set(client, 1L);
            } catch (Exception e) {
            }
        }
        return client;
    }

    private User createClient() {
        return createClient(true);
    }

    private User createEscort(boolean addId) {
        User escort = User.builder()
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
        if (addId) {
            try {
                Field escortIdField = escort.getClass().getDeclaredField("id");
                escortIdField.setAccessible(true);
                escortIdField.set(escort, 2L);
            } catch (Exception e) {
            }
        }
        return escort;
    }

    private Settlement createSettlement(Application application, User escort, Long id, SettlementStatus status) {
        int hourlyPay = 15_000;
        int hours = 10;
        int amount = hourlyPay * hours;
        int payoutAmount = (int)(amount * 0.9);
        int platformFee = (int) (amount * 0.1);

        return Settlement.builder()
                .id(id)
                .payoutAmount(payoutAmount)
                .platformFee(platformFee)
                .penaltyAmount(0)
                .settlementStatus(status)
                .settledDate(LocalDate.now().plusDays(1))
                .application(application)
                .escort(escort)
                .build();
    }

    private Application createApplication(User escort, Post post, Long id, ApplicationStatus status) {
        return Application.builder()
                .id(id)
                .post(post)
                .escort(escort)
                .status(status)
                .acceptedPostId(post.getId())
                .activePostId(post.getId())
                .build();
    }

    private EscortProfile createEscortProfile(User escort) {
        return new EscortProfile(
                escort,
                "동행 매니저입니다.",
                "오픈은행",
                "동행매니저",
                "123-000000-123");
    }

    @Test
    @DisplayName("[SettlementService] 외부 송금 실패 시 FAILED")
    void settlementThirdPartyApiFailed() {
        User escort = createEscort(false);
        EscortProfile escortProfile = createEscortProfile(escort);
        Settlement settlement = createSettlement(null, escort, null, SettlementStatus.PENDING);

        userRepository.save(escort);
        escortProfileRepository.save(escortProfile);
        settlementRepository.save(settlement);

        doThrow(new InternalServerErrorException("정산 외부 API 호출 중 에러 발생"))
                .when(settlementClient).settlementRequest(any());

        assertThatThrownBy(() -> settlementService.request(escort.getId(), settlement.getId()))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("정산에 실패했습니다.");

        em.flush();
        em.clear();

        assertThat(settlementRepository.findById(settlement.getId()).get().getSettlementStatus()).isEqualTo(SettlementStatus.FAILED);
    }

    @Test
    @DisplayName("[SettlementService] FAILED → 재시도로 PROCESSING 가능")
    void settlementRequestStatusFailed() {
        User escort = createEscort(false);
        EscortProfile escortProfile = createEscortProfile(escort);
        Settlement settlement = createSettlement(null, escort, null, SettlementStatus.FAILED);

        userRepository.save(escort);
        escortProfileRepository.save(escortProfile);
        settlementRepository.save(settlement);

        doReturn(new SettlementClientResponse("123-000000-123", "동행매니저", 150_000))
                .when(settlementClient).settlementRequest(any());

        settlementService.request(escort.getId(), settlement.getId());

        em.flush();
        em.clear();

        assertThat(settlementRepository.findById(settlement.getId()).get().getSettlementStatus())
                .isEqualTo(SettlementStatus.COMPLETED);
    }

    @Test
    @DisplayName("[SettlementService] PROCESSING/COMPLETED 재요청 거부")
    void settlementRequestStatusProcessingAndCompleted() {
        User escort = createEscort(false);
        EscortProfile escortProfile = createEscortProfile(escort);
        Settlement settlementProcessing = createSettlement(null, escort, null, SettlementStatus.PROCESSING);
        Settlement settlementCompleted = createSettlement(null, escort, null, SettlementStatus.COMPLETED);

        userRepository.save(escort);
        escortProfileRepository.save(escortProfile);
        settlementRepository.save(settlementProcessing);
        settlementRepository.save(settlementCompleted);

        assertThatThrownBy(() -> settlementService.request(escort.getId(), settlementProcessing.getId()))
                .isInstanceOf(InvalidException.class)
                .hasMessage("이미 정산 중이거나 정산이 완료되었습니다.");
        assertThatThrownBy(() -> settlementService.request(escort.getId(), settlementCompleted.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("정산 데이터를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("[SettlementService] 정산 예정일 전 요청 가능")
    void settlementEarlyRequest() {
        User escort = createEscort(false);
        EscortProfile escortProfile = createEscortProfile(escort);
        Settlement settlement = createSettlement(null, escort, null, SettlementStatus.PENDING);

        userRepository.save(escort);
        escortProfileRepository.save(escortProfile);
        settlementRepository.save(settlement);

        doReturn(new SettlementClientResponse("123-000000-123", "동행매니저", 150_000))
                .when(settlementClient).settlementRequest(any());

        settlementService.request(escort.getId(), settlement.getId());

        em.flush();
        em.clear();

        Settlement completedSettlement = settlementRepository.findById(settlement.getId()).get();
        assertThat(completedSettlement.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETED);
        assertThat(completedSettlement.getSettledDate().getDayOfMonth()).isEqualTo(LocalDate.now().plusDays(1).getDayOfMonth());
    }

    @Test
    @DisplayName("[SettlementService] 같은 지원에 정산 2번 생성 방지(unique)")
    void settlementUniqueConstraint() {
        User escort = createEscort(false);
        User client = createClient(false);
        Post post = createPost(client, null, PostStatus.COMPLETED);
        Application application = createApplication(escort, post, null, ApplicationStatus.ACCEPTED);
        Settlement settlement1 = createSettlement(application, escort, null, SettlementStatus.PENDING);
        Settlement settlement2 = createSettlement(application, escort, null, SettlementStatus.PENDING);

        userRepository.save(escort);
        userRepository.save(client);
        postRepository.save(post);
        applicationRepository.save(application);
        settlementRepository.save(settlement1);

        assertThatThrownBy(() -> settlementRepository.save(settlement2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
