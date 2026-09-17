package com.back.nbe12142team06.domain.report.repository;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EntityManager em;

    private Application application;

    @BeforeEach
    void setUp() {
        User client = new User("client1", "pw", "client1@test.com", "김의뢰",
                Role.CLIENT, Gender.FEMALE, LocalDate.of(1950, 1, 1), "01011112222", "서울");
        User escort = new User("escort1", "pw", "escort1@test.com", "박동행",
                Role.ESCORT, Gender.MALE, LocalDate.of(1995, 1, 1), "01033334444", "서울");
        em.persist(client);
        em.persist(escort);

        Post post = Post.builder()
                .client(client)
                .title("정형외과 동행")
                .content("어머니 정기 진료 동행해주실 분")
                .region("서울")
                .hospitalName("서울정형외과")
                .hospitalAddress("서울시 은평구")
                .hospitalLat(new BigDecimal("37.6176000"))
                .hospitalLng(new BigDecimal("126.9227000"))
                .pickupAddress("서울시 은평구 자택")
                .pickupLat(new BigDecimal("37.6180000"))
                .pickupLng(new BigDecimal("126.9230000"))
                .hourlyPay(15000)
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now().plusHours(12))
                .escortStartAt(LocalDateTime.now().plusDays(1))
                .escortEndAt(LocalDateTime.now().plusDays(1).plusHours(3))
                .build();
        em.persist(post);

        application = Application.builder()
                .post(post)
                .escort(escort)
                .build();
        em.persist(application);
    }

    @Test
    @DisplayName("보고서를 저장하면 생성 시각이 자동으로 채워진다")
    void 보고서_저장() {
        Report report = Report.builder()
                .application(application)
                .title("진료 결과")
                .originContent("정형외과 진료 후 물리치료 처방을 받으셨습니다.")
                .build();

        Report saved = reportRepository.save(report);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getAiSummary()).isNull();  // 요약 전이므로 null
    }

    @Test
    @DisplayName("같은 동행 건에 보고서를 두 번 저장하면 예외가 발생한다")
    void 보고서_중복_저장_예외() {
        reportRepository.save(Report.builder()
                .application(application)
                .title("첫 번째")
                .originContent("내용")
                .build());
        em.flush();

        Report duplicate = Report.builder()
                .application(application)
                .title("두 번째")
                .originContent("내용")
                .build();

        assertThatThrownBy(() -> {
            reportRepository.save(duplicate);
            em.flush();  // flush 해야 DB 제약이 실제로 걸림
        }).isInstanceOf(Exception.class);
    }
}