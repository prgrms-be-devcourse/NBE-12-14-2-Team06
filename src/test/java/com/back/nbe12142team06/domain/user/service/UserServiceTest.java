package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
public class UserServiceTest {

    private final UserService userService;
    private final UserRepository userRepository;

    @Test
    @DisplayName("회원가입 - 단순 저장")
    void t1(){
        User userOrigin = new User(
                "user1",
                "1234",
                "user@test.test",
                "유저1",
                Role.CLIENT,
                Gender.MALE,
                LocalDate.of(1990, 5, 6),
                "010-1234-5678",
                "서울"
        );
        // 저장
        this.userService.signUp(userOrigin);

        Long originId = userOrigin.getId();

        User userCheck = this.userRepository.findById(originId).get();
        assertThat(userCheck.getId()).isEqualTo(userOrigin.getId());
        assertThat(userCheck.getUsername()).isEqualTo("user1");
        assertThat(userCheck.getPassword()).isEqualTo("1234");
        assertThat(userCheck.getEmail()).isEqualTo("user@test.test");
        assertThat(userCheck.getName()).isEqualTo("유저1");
        assertThat(userCheck.getRole()).isEqualTo(Role.CLIENT);
        assertThat(userCheck.getGender()).isEqualTo(Gender.MALE);
        assertThat(userCheck.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 6));
        assertThat(userCheck.getPhoneNum()).isEqualTo("010-1234-5678");
        assertThat(userCheck.getRegion()).isEqualTo("서울");
    }
}
