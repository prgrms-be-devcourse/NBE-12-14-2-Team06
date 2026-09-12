package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final int DUPLICATED_USERNAME = 1;
    private static final int DUPLICATED_EMAIL = 2;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signUp(String username, String password, String email, String name, Role role, Gender gender, LocalDate birthDate, String phoneNum, String region) {
        // username 중복 검사
        if (this.userRepository.existsByUsername(username)) {
            throw new DuplicatedException(DUPLICATED_USERNAME, "이미 사용 중인 아이디입니다.");
        }
        // email 중복 검사
        if (this.userRepository.existsByEmail(email)) {
            throw new DuplicatedException(DUPLICATED_EMAIL, "이미 사용 중인 이메일입니다.");
        }
        // 비밀번호 암호화
        User user = new User(username, passwordEncoder.encode(password), email, name, role, gender, birthDate, phoneNum, region);
        return this.userRepository.save(user);
    }
}
