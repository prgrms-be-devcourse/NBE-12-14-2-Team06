package com.back.nbe12142team06.domain.payment.service;

import com.back.nbe12142team06.domain.payment.client.TossPaymentClient;
import com.back.nbe12142team06.domain.payment.dto.PaymentConfirmRequest;
import com.back.nbe12142team06.domain.payment.dto.TossConfirmResponse;
import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentConcurrencyTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @MockitoSpyBean
    private TossPaymentClient tossPaymentClient;

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

    private User createClient() {
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
        return client;
    }

    @Test
    @DisplayName("[PaymentService] 결제 승인 2번 동시 요청")
    void paymentConfirmConcurrency() throws Exception {
        User client = createClient();
        Post post = createPost(client, null);
        Payment payment = createPayment(post, PaymentStatus.READY, null);

        User savedClient = userRepository.save(client);
        Post savedPost = postRepository.save(post);
        Payment savedPayment = paymentRepository.save(payment);

        doReturn(ResponseEntity.ok(new TossConfirmResponse("계좌이체", String.valueOf(payment.getAmount()))))
                .when(tossPaymentClient).callApiConfirm(savedPayment.getPaymentKey(), savedPayment.getOrderId(), String.valueOf(savedPayment.getAmount()));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                latch.await();
                paymentService.confirm(
                        new PaymentConfirmRequest(savedPayment.getPaymentKey(), savedPayment.getOrderId(), String.valueOf(savedPayment.getAmount())),
                        savedPayment.getId(), savedClient.getId(), String.valueOf(savedPayment.getAmount()), savedPayment.getOrderId());
                return null;
            });
        }

        latch.countDown();
        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            executor.close();
        }

        verify(tossPaymentClient, times(1)).callApiConfirm(any(), any(), any());
    }

}
