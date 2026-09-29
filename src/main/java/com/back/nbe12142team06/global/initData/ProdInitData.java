package com.back.nbe12142team06.global.initData;

import com.back.nbe12142team06.domain.education.entity.EducationVideo;
import com.back.nbe12142team06.domain.education.repository.EducationVideoRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

/**
 * 배포(prod)용 필수 초기 데이터 생성기.
 * 배포 환경은 ddl-auto: update 라 데이터가 유지되므로, 이미 있으면 다시 만들지 않습니다(재시작해도 안전).
 *
 * 넣는 것은 딱 두 가지입니다.
 * - 관리자 계정 1개: 회원가입 API(UserService.signUp)로는 ADMIN 을 만들 수 없어서 여기서 만듭니다.
 *   계정 정보는 코드에 두지 않고 .env(환경변수) → application-prod.yml(custom.admin.*) 로 받습니다.
 * - 교육 영상 1개: 동행 매니저가 교육을 이수(verified)해야 공고에 지원할 수 있으므로 필수입니다.
 *   영상 파일은 프론트 public/videos/ 에 있어야 합니다.
 */
@Slf4j
@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class ProdInitData {

    private final UserRepository userRepository;
    private final EducationVideoRepository educationVideoRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformTransactionManager transactionManager;

    @Value("${custom.admin.username}")
    private String adminUsername;

    @Value("${custom.admin.password}")
    private String adminPassword;

    @Value("${custom.admin.email}")
    private String adminEmail;

    @Value("${custom.admin.phone-num}")
    private String adminPhoneNum;

    @Value("${custom.admin.name:관리자}")
    private String adminName;

    @Value("${custom.admin.region:서울}")
    private String adminRegion;

    @Bean
    ApplicationRunner prodInitDataApplicationRunner() {
        return args -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            transactionTemplate.executeWithoutResult(status -> {
                initAdmin();
                initEducationVideos();
            });
        };
    }

    void initAdmin() {
        // 값이 비어 있으면 빈 비밀번호 관리자가 생기지 않도록 서버 시작을 멈춥니다.
        requireNotBlank(adminUsername, "ADMIN_USERNAME");
        requireNotBlank(adminPassword, "ADMIN_PASSWORD");
        requireNotBlank(adminEmail, "ADMIN_EMAIL");
        requireNotBlank(adminPhoneNum, "ADMIN_PHONE_NUM");

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("[초기 데이터] 관리자 계정이 이미 있어 건너뜁니다. username={}", adminUsername);
            return;
        }

        // 아이디는 없는데 이메일·전화번호를 다른 회원이 쓰고 있으면 유니크 제약에 걸리므로 먼저 알려 줍니다.
        if (userRepository.existsByEmail(adminEmail) || userRepository.existsByPhoneNum(adminPhoneNum)) {
            throw new IllegalStateException("관리자 이메일 또는 전화번호를 이미 다른 회원이 사용 중입니다. .env 값을 확인해주세요.");
        }

        User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .email(adminEmail)
                .name(adminName)
                .role(Role.ADMIN)
                .gender(Gender.MALE)                    // 필수 컬럼이라 기본값으로 채웁니다.
                .birthDate(LocalDate.of(1985, 1, 1))    // 필수 컬럼이라 기본값으로 채웁니다.
                .phoneNum(adminPhoneNum)
                .region(adminRegion)
                .build();
        userRepository.save(admin);

        // 비밀번호는 로그에 남기지 않습니다.
        log.info("[초기 데이터] 관리자 계정을 만들었습니다. username={}", adminUsername);
    }

    void initEducationVideos() {
        if (educationVideoRepository.count() > 0) {
            log.info("[초기 데이터] 교육 영상이 이미 있어 건너뜁니다.");
            return;
        }

        educationVideoRepository.save(
                new EducationVideo("동행 서비스 기본 교육", "/videos/sample_video.mp4", 60, true)
        );
        log.info("[초기 데이터] 교육 영상을 등록했습니다.");
    }

    private static void requireNotBlank(String value, String envName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(envName + " 환경변수가 비어 있습니다. .env 를 확인해주세요.");
        }
    }
}