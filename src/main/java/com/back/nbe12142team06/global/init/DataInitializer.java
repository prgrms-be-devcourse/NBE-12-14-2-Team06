package com.back.nbe12142team06.global.init;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Profile({"dev", "test"})
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        if (postRepository.count() > 0) return;

        // 유저 먼저 생성
        User client1 = userRepository.save(new User(
                "client1", "1234", "client1@test.com",
                "의뢰인1", Role.CLIENT, Gender.FEMALE,
                LocalDate.of(1985, 3, 15), "010-1111-1111", "서울"
        ));

        User client2 = userRepository.save(new User(
                "client2", "1234", "client2@test.com",
                "의뢰인2", Role.CLIENT, Gender.MALE,
                LocalDate.of(1970, 7, 22), "010-2222-2222", "부산"
        ));

        User escort1 = userRepository.save(new User(
                "escort1", "1234", "escort1@test.com",
                "동행인1", Role.ESCORT, Gender.MALE,
                LocalDate.of(1970, 7, 22), "010-3333-2222", "부산"
        ));
        User escort2 = userRepository.save(new User(
                "escort2", "1234", "escort2@test.com",
                "동행인2", Role.ESCORT, Gender.MALE,
                LocalDate.of(1970, 7, 22), "010-4444-2222", "부산"
        ));
        User admin1 = userRepository.save(new User(
                "admin1", "1234", "admin1@test.com",
                "관리자1", Role.ADMIN, Gender.MALE,
                LocalDate.of(1970, 7, 22), "010-5555-2222", "부산"
        ));

        // 공고 3개 생성
        // 공고1
        postRepository.save(Post.builder()
                .client(client1)
                .title("정형외과 동행 구합니다")
                .content("무릎 수술 후 검진 예약이 있어 동행인이 필요합니다.")
                .patientNote("거동이 불편하신 70대 어르신")
                .region("서울")
                .hospitalName("서울성모병원")
                .hospitalAddress("서울 서초구 반포대로 222")
                .hospitalLat(new BigDecimal("37.5012743"))
                .hospitalLng(new BigDecimal("127.0051893"))
                .pickupAddress("서울 서초구 잠원동 10-1")
                .pickupLat(new BigDecimal("37.5160000"))
                .pickupLng(new BigDecimal("127.0200000"))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now().plusDays(1))
                .recruitEndAt(LocalDateTime.now().plusDays(2))
                .escortStartAt(LocalDateTime.now().plusDays(3))
                .escortEndAt(LocalDateTime.now().plusDays(3).plusHours(3))
                .build());

// 공고2
        postRepository.save(Post.builder()
                .client(client2)
                .title("내과 진료 동행 부탁드립니다")
                .content("당뇨 정기검진이 있어 도움이 필요합니다.")
                .patientNote("인슐린 주사 필요")
                .region("부산")
                .hospitalName("부산대학교병원")
                .hospitalAddress("부산 서구 구덕로 179")
                .hospitalLat(new BigDecimal("35.1060000"))
                .hospitalLng(new BigDecimal("129.0210000"))
                .pickupAddress("부산 서구 아미동 1가")
                .pickupLat(new BigDecimal("35.1070000"))
                .pickupLng(new BigDecimal("129.0150000"))
                .hourlyPay(12000)
                .recruitStartAt(LocalDateTime.now().plusDays(3))
                .recruitEndAt(LocalDateTime.now().plusDays(4))
                .escortStartAt(LocalDateTime.now().plusDays(5))
                .escortEndAt(LocalDateTime.now().plusDays(5).plusHours(2))
                .build());

// 공고3
        postRepository.save(Post.builder()
                .client(client1)
                .title("안과 수술 후 귀가 동행 구해요")
                .content("백내장 수술 후 혼자 귀가가 어려워 동행인이 필요합니다.")
                .region("대구")
                .hospitalName("대구가톨릭대학교병원")
                .hospitalAddress("대구 남구 두류공원로17길 33")
                .hospitalLat(new BigDecimal("35.8560000"))
                .hospitalLng(new BigDecimal("128.5630000"))
                .pickupAddress("대구 남구 이천동 200")
                .pickupLat(new BigDecimal("35.8480000"))
                .pickupLng(new BigDecimal("128.5700000"))
                .hourlyPay(13000)
                .recruitStartAt(LocalDateTime.now().plusDays(5))
                .recruitEndAt(LocalDateTime.now().plusDays(6))
                .escortStartAt(LocalDateTime.now().plusDays(7))
                .escortEndAt(LocalDateTime.now().plusDays(7).plusHours(4))
                .build());
    }
}