package com.back.nbe12142team06.global.initData;

import com.back.nbe12142team06.domain.payment.entity.Payment;
import com.back.nbe12142team06.domain.payment.entity.PaymentStatus;
import com.back.nbe12142team06.domain.payment.repository.PaymentRepository;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * 개발용 초기 데이터 생성기.
 * dev 프로필로 서버가 뜰 때(= application-dev.yaml, ddl-auto: create 로 매번 스키마가 새로 만들어질 때)
 * 공고 목록 화면을 바로 확인할 수 있도록 의뢰인 3명 + 공고 15건 + 결제 15건을 만들어 둡니다.
 *
 * 구성 원칙 (프론트 데모/PPT 용으로 신경 쓴 부분들):
 * - 목록 조회 API 는 결제(Payment.paymentStatus = DONE) 가 있는 공고만 보여주므로, 공고마다 결제를 하나씩 만듭니다.
 * - "모집중" 탭(OPEN)이 "마감" 탭보다 항상 더 많이 보이도록: OPEN 11건(그 중 3건은 결제 대기 상태라 목록엔 안 보임 → 실제 노출 8건) vs
 *   OPEN 아님 4건(전부 결제완료 → 노출 4건). 8 > 4.
 * - "기간 = 오늘" 필터로 체크해도 항상 하나는 걸리도록, 동행시작시간을 LocalDateTime.now() 로 잡은 공고를 하나 넣어둡니다.
 *   (서버를 언제 재시작하든 그 시점의 "오늘"로 다시 세팅됩니다.)
 * - 시급은 10,000 ~ 14,000원 사이로 다양하게 분포시킵니다.
 * - 전부 "방금 전"으로만 보이지 않도록, 등록일(createdAt)을 분/시간/일 단위로 다르게 backdate 합니다.
 *   createdAt 은 @CreatedDate(updatable = false) 라 엔티티 빌더로는 못 바꾸므로, 저장 후 네이티브 UPDATE 로 직접 보정합니다.
 */
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class BaseInitData {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager em;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        // initPosts() 내부의 네이티브 UPDATE(createdAt 보정)는 활성 트랜잭션이 필요한데,
        // 여기서 this.initPosts() 로 자가 호출하면 프록시를 거치지 않아 @Transactional 을 붙여도 적용되지 않습니다
        // (Spring AOP self-invocation 한계). 그래서 TransactionTemplate 으로 initPosts() 전체를 감싸서
        // 실제 트랜잭션 안에서 실행되도록 합니다.
        return args -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.executeWithoutResult(status -> initPosts());
        };
    }

    void initPosts() {
        if (userRepository.count() > 0) return; // 이미 데이터가 있으면 다시 만들지 않음 (안전장치)

        List<User> clients = List.of(
                createClient("client01", "김의뢰", "010-1000-0001", "서울"),
                createClient("client02", "이의뢰", "010-1000-0002", "부산"),
                createClient("client03", "박의뢰", "010-1000-0003", "경기")
        );

        record PostSeed(
                String title, String content, String patientNote,
                String region, String hospitalName, String hospitalAddress,
                BigDecimal hospitalLat, BigDecimal hospitalLng,
                String pickupAddress, BigDecimal pickupLat, BigDecimal pickupLng,
                int hourlyPay,
                LocalDateTime recruitStartAt, LocalDateTime recruitEndAt,
                LocalDateTime escortStartAt, LocalDateTime escortEndAt,
                PostStatus postStatus,
                int createdAtMinutesAgo   // 등록일(createdAt)을 이만큼 과거로 보정 (0 = 방금 등록된 그대로 둠)
        ) {}

        List<PostSeed> seeds = List.of(
                // ── 0: 모집중(OPEN) — 2시간 전 등록 ──
                new PostSeed(
                        "강남서울병원 진료 동행 부탁드립니다", "정형외과 진료입니다.\n휠체어 이동 보조가 필요해요.", "무릎 수술 후 거동 불편",
                        "서울", "강남서울병원", "서울 강남구 테헤란로 231",
                        bd("37.5012"), bd("127.0396"),
                        "서울 강남구 역삼로 12 (자택)", bd("37.4995"), bd("127.0362"),
                        11000,
                        now(), plusDays(3, 18, 0), plusDays(4, 9, 0), plusDays(4, 12, 0),
                        PostStatus.OPEN, 120
                ),
                // ── 1: 모집중(OPEN) — 결제 대기(READY) 테스트용, 목록엔 노출 안 됨 ──
                new PostSeed(
                        "해운대백병원 정기검진 동행", "정기 건강검진 동행입니다.\n오전 접수 후 오후 귀가 예정.", null,
                        "부산", "해운대백병원", "부산 해운대구 해운대로 875",
                        bd("35.1885"), bd("129.1615"),
                        "부산 해운대구 좌동순환로 5 (자택)", bd("35.1701"), bd("129.1697"),
                        12500,
                        now(), plusDays(4, 18, 0), plusDays(5, 9, 30), plusDays(5, 12, 30),
                        PostStatus.OPEN, 300
                ),
                // ── 2: 모집중(OPEN) — 1일 전 등록 ──
                new PostSeed(
                        "대전성모병원 재활치료 동행", "재활의학과 물리치료 동행입니다.\n보행 보조기 사용 중.", "보행 보조기 사용",
                        "대전", "대전성모병원", "대전 서구 둔산로 100",
                        bd("36.3527"), bd("127.3789"),
                        "대전 서구 계룡로 20 (자택)", bd("36.3488"), bd("127.3821"),
                        10500,
                        now(), plusDays(6, 18, 0), plusDays(7, 10, 0), plusDays(7, 14, 0),
                        PostStatus.OPEN, 1440
                ),
                // ── 3: 모집중(OPEN) — 10분 전 등록 ──
                new PostSeed(
                        "분당서울대병원 항암 통원치료 동행", "항암 통원치료 동행입니다.\n치료 후 컨디션 저하 가능하여 세심한 보조 부탁드려요.", "항암 치료 중, 어지럼증 있음",
                        "경기", "분당서울대병원", "경기 성남시 판교로 145",
                        bd("37.3512"), bd("127.1256"),
                        "경기 성남시 야탑로 8 (자택)", bd("37.4106"), bd("127.1289"),
                        14000,
                        now(), plusDays(2, 18, 0), plusDays(3, 9, 0), plusDays(3, 13, 0),
                        PostStatus.OPEN, 10
                ),
                // ── 4: 모집중(OPEN) — 결제 대기(READY) 테스트용, 목록엔 노출 안 됨 ──
                new PostSeed(
                        "광주기독병원 외래 진료 동행", "내과 외래 진료 동행입니다.\n대중교통 이용 예정.", null,
                        "광주", "광주기독병원", "광주 서구 상무대로 250",
                        bd("35.1499"), bd("126.8815"),
                        "광주 서구 죽봉대로 15 (자택)", bd("35.1523"), bd("126.8901"),
                        10000,
                        now(), plusDays(7, 18, 0), plusDays(8, 9, 0), plusDays(8, 11, 0),
                        PostStatus.OPEN, 480
                ),
                // ── 5: 모집중(OPEN) — 2일 전 등록 ──
                new PostSeed(
                        "인천사랑병원 순환기내과 진료 동행", "정기 순환기내과 진료입니다.\n혈압 체크 후 약 처방받는 일정입니다.", null,
                        "인천", "인천사랑병원", "인천 남동구 인주대로 100",
                        bd("37.4487"), bd("126.7320"),
                        "인천 남동구 인주대로 30 (자택)", bd("37.4463"), bd("126.7280"),
                        13000,
                        now(), plusDays(6, 18, 0), plusDays(6, 9, 0), plusDays(6, 11, 0),
                        PostStatus.OPEN, 2880
                ),
                // ── 6: 모집중(OPEN) — 결제 대기(READY) 테스트용, 목록엔 노출 안 됨 ──
                new PostSeed(
                        "청주성모병원 정형외과 진료 동행", "무릎 관절 정기 진료입니다.\n계단 이용이 어려워 엘리베이터 동선으로 부탁드려요.", "무릎 관절염으로 계단 이용 어려움",
                        "충청북도", "청주성모병원", "충청북도 청주시 흥덕로 200",
                        bd("36.6357"), bd("127.4913"),
                        "충청북도 청주시 사직대로 15 (자택)", bd("36.6392"), bd("127.4875"),
                        11500,
                        now(), plusDays(9, 18, 0), plusDays(9, 9, 0), plusDays(9, 12, 0),
                        PostStatus.OPEN, 4320
                ),
                // ── 7: 모집중(OPEN) — 5일 전 등록 ──
                new PostSeed(
                        "전남대학교병원(화순) 항암 통원치료 동행", "항암 통원치료 동행입니다.\n치료 후 컨디션 저하 가능성 있어 보조 부탁드립니다.", "항암 치료 중",
                        "전라남도", "전남대학교병원", "전라남도 화순군 화순읍 대학로 322",
                        bd("35.0587"), bd("126.9866"),
                        "전라남도 화순군 화순읍 노인로 5 (자택)", bd("35.0612"), bd("126.9821"),
                        13500,
                        now(), plusDays(10, 18, 0), plusDays(10, 9, 0), plusDays(10, 11, 0),
                        PostStatus.OPEN, 7200
                ),
                // ── 8: 모집중(OPEN) + 오늘 마감 → "오늘 마감" 뱃지, 20분 전 등록 ──
                new PostSeed(
                        "마포연세병원 안과 진료 동행", "백내장 수술 후 경과 관찰 진료입니다.\n택시 동승 부탁드려요.", null,
                        "서울", "마포연세병원", "서울 마포구 월드컵로 212",
                        bd("37.5636"), bd("126.9096"),
                        "서울 마포구 성산로 30 (자택)", bd("37.5601"), bd("126.9145"),
                        12000,
                        now(), today2359(), plusDays(1, 9, 0), plusDays(1, 11, 30),
                        PostStatus.OPEN, 20
                ),
                // ── 9: 모집중(OPEN) + 오늘 마감 → "오늘 마감" 뱃지, 2일 전 등록 ──
                new PostSeed(
                        "인천길병원 신장내과 투석 동행", "정기 투석 치료 동행입니다.\n왕복 동행 부탁드립니다.", "만성 신장질환으로 정기 투석 중",
                        "인천", "인천길병원", "인천 남동구 남동대로 774",
                        bd("37.4487"), bd("126.7316"),
                        "인천 남동구 인주대로 8 (자택)", bd("37.4463"), bd("126.7280"),
                        10500,
                        now(), today2359(), plusDays(2, 8, 30), plusDays(2, 11, 0),
                        PostStatus.OPEN, 2880
                ),
                // ── 10: 모집중(OPEN) — 동행시작이 "지금(now())" → 기간=오늘 필터에 항상 걸림, 1시간 전 등록 ──
                new PostSeed(
                        "서울베스트병원 오늘 출발 진료 동행 (급구)", "오늘 바로 출발하는 진료 동행입니다.\n빠른 매칭 부탁드려요.", null,
                        "서울", "서울베스트병원", "서울 영등포구 여의대로 24",
                        bd("37.5219"), bd("126.9245"),
                        "서울 영등포구 국제금융로 10 (자택)", bd("37.5250"), bd("126.9260"),
                        13000,
                        nowMinusHours(1), plusDays(2, 18, 0), now(), nowPlusHours(2),
                        PostStatus.OPEN, 60
                ),
                // ── 11~14: OPEN 이 아닌 상태 → "마감" 탭, 각각 4/6/8/12일 전 등록 ──
                new PostSeed(
                        "대구가톨릭대병원 정형외과 진료 동행", "고관절 수술 후 경과 관찰입니다.\n휠체어 이동 지원 필요.", "고관절 수술 후 재활 중",
                        "대구", "대구가톨릭대병원", "대구 수성구 달구벌대로 3056",
                        bd("35.8463"), bd("128.6335"),
                        "대구 수성구 동대구로 50 (자택)", bd("35.8523"), bd("128.6280"),
                        11000,
                        minusDays(3, 9, 0), minusDays(1, 18, 0), plusDays(2, 9, 0), plusDays(2, 12, 0),
                        PostStatus.MATCHED, 5760
                ),
                new PostSeed(
                        "울산대병원 응급 후속 진료 동행", "응급실 방문 후 후속 진료 동행입니다.\n현재 동행 진행 중입니다.", null,
                        "울산", "울산대병원", "울산 남구 삼산로 100",
                        bd("35.5390"), bd("129.3159"),
                        "울산 남구 남산로 12 (자택)", bd("35.5421"), bd("129.3103"),
                        10000,
                        minusDays(4, 9, 0), minusDays(2, 18, 0), today(9, 0), today(11, 0),
                        PostStatus.IN_PROGRESS, 8640
                ),
                new PostSeed(
                        "아주대병원 심장내과 검사 동행", "심장 정밀검사 동행이었습니다.\n무사히 검사 마치고 귀가했습니다.", null,
                        "경기", "아주대병원", "경기 수원시 월드컵로 164",
                        bd("37.2802"), bd("127.0435"),
                        "경기 수원시 인계로 20 (자택)", bd("37.2751"), bd("127.0289"),
                        13500,
                        minusDays(6, 9, 0), minusDays(4, 18, 0), minusDays(2, 9, 0), minusDays(2, 12, 0),
                        PostStatus.COMPLETED, 11520
                ),
                new PostSeed(
                        "송파병원 소화기내과 진료 동행", "내시경 검사 동행 예정이었으나 의뢰인 사정으로 취소되었습니다.", null,
                        "서울", "송파병원", "서울 송파구 올림픽로 345",
                        bd("37.5145"), bd("127.1052"),
                        "서울 송파구 잠실로 8 (자택)", bd("37.5100"), bd("127.0980"),
                        12500,
                        minusDays(5, 9, 0), minusDays(3, 18, 0), plusDays(1, 9, 0), plusDays(1, 12, 0),
                        PostStatus.CANCELED, 17280
                )
        );

        // 결제 대기(READY) 상태로 남겨둘 공고 인덱스 (나중에 결제 완료 처리 테스트용, 3건)
        // 전부 "모집중" 쪽 공고 중에서 고릅니다 — "오늘 마감"/"오늘 출발" 데모용 공고는 제외해서 항상 목록에 뜨게 합니다.
        Set<Integer> readyPaymentIndexes = Set.of(1, 4, 6);

        for (int i = 0; i < seeds.size(); i++) {
            PostSeed seed = seeds.get(i);
            User client = clients.get(i % clients.size());

            Post post = Post.builder()
                    .client(client)
                    .title(seed.title())
                    .content(seed.content())
                    .patientNote(seed.patientNote())
                    .region(seed.region())
                    .hospitalName(seed.hospitalName())
                    .hospitalAddress(seed.hospitalAddress())
                    .hospitalLat(seed.hospitalLat())
                    .hospitalLng(seed.hospitalLng())
                    .pickupAddress(seed.pickupAddress())
                    .pickupLat(seed.pickupLat())
                    .pickupLng(seed.pickupLng())
                    .hourlyPay(seed.hourlyPay())
                    .recruitStartAt(seed.recruitStartAt())
                    .recruitEndAt(seed.recruitEndAt())
                    .escortStartAt(seed.escortStartAt())
                    .escortEndAt(seed.escortEndAt())
                    .postStatus(seed.postStatus())
                    .build();
            postRepository.save(post);

            // 목록 API 가 "결제 완료(DONE)" 된 공고만 보여주므로, 공고마다 결제를 하나씩 만들어 둡니다.
            // 단, readyPaymentIndexes 에 해당하는 3건은 결제 대기(READY) 상태로 남겨서
            // "결제 완료 처리" 테스트용으로 씁니다. (이 3건은 목록 API 에는 노출되지 않습니다.)
            boolean isReady = readyPaymentIndexes.contains(i);
            PaymentStatus paymentStatus = isReady ? PaymentStatus.READY : PaymentStatus.DONE;

            Payment.PaymentBuilder paymentBuilder = Payment.builder()
                    .amount(post.getTotalPay().intValue())
                    .hourlyPaySnapshot(post.getHourlyPay())
                    .hours(post.getEscortHours())
                    .method("카드")
                    .paymentStatus(paymentStatus)
                    .post(post);

            if (!isReady) {
                // 결제 승인 완료 시 채워지는 값들 (READY 상태에서는 아직 없어야 자연스러움)
                paymentBuilder
                        .orderId("INIT-ORDER-%02d".formatted(i + 1))
                        .paymentKey("INIT-PAYKEY-%02d".formatted(i + 1))
                        .approvedAt(LocalDateTime.now())
                        .balanceAmount(post.getTotalPay().intValue());
            }

            paymentRepository.save(paymentBuilder.build());

            // createdAt 은 @CreatedDate(updatable = false) 라 엔티티/빌더로는 수정이 안 되므로,
            // 등록일을 다양하게 보여주기 위해 저장 직후 네이티브 쿼리로 직접 보정합니다.
            if (seed.createdAtMinutesAgo() > 0) {
                em.createNativeQuery("UPDATE post SET created_at = :createdAt WHERE id = :id")
                        .setParameter("createdAt", LocalDateTime.now().minusMinutes(seed.createdAtMinutesAgo()))
                        .setParameter("id", post.getId())
                        .executeUpdate();
            }
        }
    }

    private User createClient(String username, String name, String phoneNum, String region) {
        User client = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password1!"))
                .email(username + "@example.com")
                .name(name)
                .role(Role.CLIENT)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1955, 3, 10))
                .phoneNum(phoneNum)
                .region(region)
                .build();
        return userRepository.save(client);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static LocalDateTime now() {
        return LocalDateTime.now();
    }

    private static LocalDateTime nowPlusHours(int hours) {
        return LocalDateTime.now().plusHours(hours);
    }

    private static LocalDateTime nowMinusHours(int hours) {
        return LocalDateTime.now().minusHours(hours);
    }

    private static LocalDateTime plusDays(int days, int hour, int minute) {
        return LocalDate.now().plusDays(days).atTime(LocalTime.of(hour, minute));
    }

    private static LocalDateTime minusDays(int days, int hour, int minute) {
        return LocalDate.now().minusDays(days).atTime(LocalTime.of(hour, minute));
    }

    private static LocalDateTime today(int hour, int minute) {
        return LocalDate.now().atTime(LocalTime.of(hour, minute));
    }

    /** 오늘 23:59 — 프론트 daysUntil() 계산상 "오늘 마감"(closing 뱃지)이 되도록 */
    private static LocalDateTime today2359() {
        return LocalDate.now().atTime(23, 59);
    }
}
