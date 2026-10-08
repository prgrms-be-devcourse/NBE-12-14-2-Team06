package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("[PaymentService] 공고 삭제 시 결제는 취소 상태로 변경")
    void cancelPostAndPayment() throws NoSuchFieldException, IllegalAccessException {

        User client = createClient();
        Post post = createPost(client);
        Payment payment = createPayment(post);

        when(paymentRepository.findByPostId(any(Long.class)))
                .thenReturn(Optional.of(payment));

        Payment deletedPayment = paymentService.cancelPostAndPayment(post.getId());

        assertThat(deletedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.DELETED);
    }

    private Payment createPayment(Post post) {
        int hourlyPaySnapshot = 15_000;
        BigDecimal hours = BigDecimal.TEN;
        int amount = hourlyPaySnapshot * hours.intValue();
        Payment payment = Payment.builder()
                .id(1L)
                .post(post)
                .amount(amount)
                .hourlyPaySnapshot(hourlyPaySnapshot)
                .hours(hours)
                .orderId(UUID.randomUUID().toString())
                .paymentKey(UUID.randomUUID().toString())
                .method("CARD")
                .paymentStatus(PaymentStatus.DONE)
                .approvedAt(LocalDateTime.now().minusDays(2))
                .balanceAmount(amount)
                .build();
        return payment;
    }

    private Post createPost(User client) {
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
        LocalDateTime escortEndAt = LocalDateTime.now().plusDays(7).plusHours(4);

        Post post = Post.builder()
                .id(1L)
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

    private User createClient() throws NoSuchFieldException, IllegalAccessException {
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
        Field clientIdField = client.getClass().getDeclaredField("id");
        clientIdField.setAccessible(true);
        clientIdField.set(client, 1L);
        return client;
    }

    private User createEscort() throws NoSuchFieldException, IllegalAccessException {
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
        Field escortIdField = escort.getClass().getDeclaredField("id");
        escortIdField.setAccessible(true);
        escortIdField.set(escort, 2L);
        return escort;
    }
}