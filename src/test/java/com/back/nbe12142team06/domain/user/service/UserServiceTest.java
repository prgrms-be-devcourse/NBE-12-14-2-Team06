package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    @DisplayName("[UserService] 회원가입 - 단순 저장")
    void t1(){

        User saved = this.userService.signUp("user1",
                "1234",
                "user@test.test",
                "유저1",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 5, 6),
                "010-1234-5678",
                "서울");

        // 캐시 비우고 실제로 DB에서 조회
        em.flush();
        em.clear();

        User userCheck = this.userRepository.findByUsername("user1").orElseThrow();
        assertThat(userCheck.getUsername()).isEqualTo("user1");
        assertThat(userCheck.getPassword()).isNotNull();
        assertThat(userCheck.getEmail()).isEqualTo("user@test.test");
        assertThat(userCheck.getName()).isEqualTo("유저1");
        assertThat(userCheck.getRole()).isEqualTo(Role.CLIENT);
        assertThat(userCheck.getGender()).isEqualTo(Gender.MALE);
        assertThat(userCheck.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 6));
        assertThat(userCheck.getPhoneNum()).isEqualTo("010-1234-5678");
        assertThat(userCheck.getRegion()).isEqualTo("서울");
    }

    @Test
    @DisplayName("[UserService] 회원가입 - 이미 사용 중인 아이디로 가입 시 예외")
    void t2(){
        // username = user1
        User saved = this.userService.signUp("user1",
                "1234",
                "user@test.test",
                "유저1",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 5, 6),
                "010-1234-5678",
                "서울");

        // username = user1
        DuplicatedException e = catchThrowableOfType(() ->
                this.userService.signUp("user1",
                        "1234",
                        "userA@test.test",
                        "유저A",
                        Role.CLIENT,
                        Gender.MALE,
                        LocalDate.of(1990, 5, 6),
                        "010-2345-6789",
                        "경기"),
                DuplicatedException.class
        );

        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("이미 사용 중인 아이디입니다.");
        assertThat(e.getStatusCode()).isEqualTo("409-1");
    }

    @Test
    @DisplayName("[UserService] 회원가입 - 이미 사용 중인 이메일로 가입 시 예외")
    void t3(){
        // email = user@test.test
        User saved = this.userService.signUp("user1",
                "1234",
                "user@test.test",
                "유저1",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 5, 6),
                "010-1234-5678",
                "서울");

        // email = user@test.test
        DuplicatedException e = catchThrowableOfType(() ->
                this.userService.signUp("user2",
                        "1234",
                        "user@test.test",
                        "유저A",
                        Role.CLIENT,
                        Gender.MALE,
                        LocalDate.of(1990, 5, 6),
                        "010-2345-6789",
                        "경기"),
                DuplicatedException.class
        );

        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
        assertThat(e.getStatusCode()).isEqualTo("409-2");
    }

    @Test
    @DisplayName("[UserService] 회원가입 - 비밀번호 암호화")
    void t4(){
        User saved = this.userService.signUp("user1",
                "1234",
                "user@test.test",
                "유저1",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 5, 6),
                "010-1234-5678",
                "서울");

        em.flush();
        em.clear();

        User userCheck = this.userRepository.findByUsername("user1").orElseThrow();

        // 비밀번호가 평문인지 검증
        assertThat(userCheck.getPassword()).isNotEqualTo("1234");
        // 비밀번호가 지정한 방식으로 정확히 암호화되어 저장되어 있는지 검증
        assertThat(passwordEncoder.matches("1234", userCheck.getPassword())).isTrue();
    }
}
