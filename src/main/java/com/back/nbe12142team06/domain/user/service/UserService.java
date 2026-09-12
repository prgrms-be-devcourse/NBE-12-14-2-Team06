package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final int DUPLICATED_USERNAME = 1;
    private static final int DUPLICATED_EMAIL = 2;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signUp(UserSignUpRequest request) {
        // username 중복 검사
        if (this.userRepository.existsByUsername(request.username())) {
            throw new DuplicatedException(DUPLICATED_USERNAME, "이미 사용 중인 아이디입니다.");
        }
        // email 중복 검사
        if (this.userRepository.existsByEmail(request.email())) {
            throw new DuplicatedException(DUPLICATED_EMAIL, "이미 사용 중인 이메일입니다.");
        }
        // 비밀번호 암호화
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email(),
                request.name(),
                request.role(),
                request.gender(),
                request.birthDate(),
                request.phoneNum(),
                request.region()
        );
        return this.userRepository.save(user);
    }
}
