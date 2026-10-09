package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.payment.client.TossPaymentClient;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.settlement.service.SettlementService;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.InvalidException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @MockitoSpyBean
    private PaymentRepository paymentRepository;
    @MockitoBean
    private TossPaymentClient tossPaymentClient;
    @MockitoSpyBean
    private PaymentPersistenceService paymentPersistenceService;
    @MockitoBean
    private SettlementService settlementService;

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

    private Post createPost(User client, Long id, int hour) {
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
                .client(client)
                .build();

        return post;
    }
    private Post createPost(User client, Long id) {
        return createPost(client, id, 10);
    }

    private Post createPost(User client) {
        return createPost(client, 1L, 10);
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


    private User createEscort() {
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
        try {
            Field escortIdField = escort.getClass().getDeclaredField("id");
            escortIdField.setAccessible(true);
            escortIdField.set(escort, 2L);
        } catch (Exception e) {
        }
        return escort;
    }

    @Test
    @DisplayName("[PaymentService] 공고 삭제 시 결제는 취소 상태로 변경")
    void cancelPostAndPayment() {

        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post, PaymentStatus.DONE);

        when(paymentRepository.findByPostId(any(Long.class)))
                .thenReturn(Optional.of(payment));

        Payment deletedPayment = paymentService.cancelPostAndPayment(post.getId());

        assertThat(deletedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.DELETED);
    }

    @Test
    @DisplayName("[PaymentService] 결제 상태 DELETED 결제 취소 스케줄러 성공")
    void paymentStatusDeletedToCancelSuccess() {

        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post, PaymentStatus.DELETED);

        doReturn(List.of(payment))
                .when(paymentPersistenceService).findDeletedAll();
        doReturn(Optional.of(payment))
                .when(paymentRepository).findById(any());

        int[] counts = paymentService.cancelPostAndPaymentCallApi();

        assertThat(counts[0]).isEqualTo(1);
        assertThat(counts[1]).isEqualTo(1);
        assertThat(counts[2]).isEqualTo(0);
    }

    @Test
    @DisplayName("[PaymentService] 결제 상태 DELETED 결제 취소 스케줄러 실패 - 외부 API 호출")
    void paymentStatusDeletedToCancelFailedThirdParty() {

        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post, PaymentStatus.DELETED);

        when(paymentPersistenceService.findDeletedAll())
                .thenReturn(List.of(payment));
        when(tossPaymentClient.callApiCancel(any(), any(), any()))
                .thenThrow(new RuntimeException("토스 페이먼츠 API 호출 실패"));

        int[] counts = paymentService.cancelPostAndPaymentCallApi();

        assertThat(counts[0]).isEqualTo(1);
        assertThat(counts[1]).isEqualTo(0);
        assertThat(counts[2]).isEqualTo(1);
    }

    @Test
    @DisplayName("[PaymentService] 결제 상태 DELETED 결제 취소 스케줄러 실패 - DB 저장")
    void paymentStatusDeletedToCancelFailedDb() {

        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post, PaymentStatus.DELETED);

        doReturn(List.of(payment))
                .when(paymentPersistenceService).findDeletedAll();
        doThrow(new RuntimeException("DB 저장 실패"))
                .when(paymentPersistenceService).paymentCancelDb(any(), any(), any(boolean.class));

        int[] counts = paymentService.cancelPostAndPaymentCallApi();

        assertThat(counts[0]).isEqualTo(1);
        assertThat(counts[1]).isEqualTo(0);
        assertThat(counts[2]).isEqualTo(1);
    }

    @Test
    @DisplayName("[PaymentService] 결제 재승인 시 실패")
    void paymentReConfirmFailed() {
        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post, PaymentStatus.DONE);

        doReturn(payment)
                .when(paymentPersistenceService).findById(any(Long.class), any(Long.class));

        assertThatThrownBy(() -> paymentService.confirm(null, payment.getId(), client.getId(), null, null))
                .isInstanceOf(InvalidException.class)
                .hasMessage("이미 결제를 완료하셨습니다.");
    }

    @Test
    @DisplayName("[PaymentService] CANCELED 결제 재승인 → 거부")
    void paymentReConfirmCanceledStatus() {
        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post, PaymentStatus.CANCELED);

        doReturn(payment)
                .when(paymentPersistenceService).findById(any(Long.class), any(Long.class));

        assertThatThrownBy(() -> paymentService.confirm(null, payment.getId(), client.getId(), null, null))
                .isInstanceOf(InvalidException.class)
                .hasMessage("취소된 결제입니다.");
    }

    @Test
    @DisplayName("[PaymentService] 승인 도중 IN_PROGRESS가 DB에 저장되는지")
    void paymentConfirmSavedDbInProgress() {
        User client = createClient(false);
        Post post = createPost(client, null);
        Payment payment = createPayment(post, PaymentStatus.READY, null);

        userRepository.save(client);
        postRepository.save(post);
        paymentRepository.save(payment);

        int updatedCount = paymentPersistenceService.confirmUpdateStatus(payment.getId());
        Payment inProgressPayment = paymentRepository.findById(payment.getId()).get();

        assertThat(updatedCount).isEqualTo(1);
        assertThat(inProgressPayment.getPaymentStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("[PaymentService] 결제 상태 CANCELED에서 결제 취소 요청")
    void paymentReCancelFailed() {
        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post, PaymentStatus.CANCELED);

        assertThatThrownBy(() -> paymentService.cancel(payment, null, 0, null))
                .isInstanceOf(InvalidException.class)
                .hasMessage("이미 취소된 결제입니다.");
    }

    @Test
    @DisplayName("[PaymentService] READY(미결제) 결제 취소")
    void cancelStatusReady() {
        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post, PaymentStatus.READY);

        assertThatThrownBy(() -> paymentService.cancel(payment, null, 0, null))
                .isInstanceOf(InvalidException.class)
                .hasMessage("결제 완료 상태가 아닙니다.");
    }

    @Test
    @DisplayName("[PaymentService] 동행 시간 단축 시 차액 부분 취소(PARTIAL_CANCELED)")
    void paymentPartialCanceled() {
        User client = createClient();
        Post post = createPost(client, 1L, 4);
        Payment payment = createPayment(post, PaymentStatus.DONE);

        doReturn(List.of(payment))
                .when(paymentPersistenceService).findByPostId(any());
        doReturn(null).when(paymentPersistenceService).paymentPartialCancelDb(any(), any(), any(int.class));

        paymentService.validPayment(client.getId(), post, Application.builder().build(), LocalDate.now());

        verify(paymentPersistenceService, times(1)).paymentPartialCancelDb(any(), any(), any(int.class));
    }
}