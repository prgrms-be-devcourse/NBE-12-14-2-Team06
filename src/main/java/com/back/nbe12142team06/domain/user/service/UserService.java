package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.dto.login.common.UserLoginRequest;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.BusinessException;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import com.back.nbe12142team06.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final int DUPLICATED_USERNAME = 1;
    private static final int DUPLICATED_EMAIL = 2;
    private static final int DUPLICATED_PHONE_NUM = 3;

    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public User signUp(UserSignUpRequest request) {
        // Admin으로 가입 불가
        if (request.role() == Role.ADMIN) {
            throw new BusinessException("400-3", "잘못된 요청입니다.");
        }

        // username 중복 검사
        if (this.userRepository.existsByUsername(request.username())) {
            throw new DuplicatedException(DUPLICATED_USERNAME, "이미 사용 중인 아이디입니다.");
        }
        // email 중복 검사
        if (this.userRepository.existsByEmail(request.email())) {
            throw new DuplicatedException(DUPLICATED_EMAIL, "이미 사용 중인 이메일입니다.");
        }
        // username 중복 검사
        if (this.userRepository.existsByPhoneNum(request.phoneNum())) {
            throw new DuplicatedException(DUPLICATED_PHONE_NUM, "이미 사용 중인 전화번호입니다.");
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

    public User myProfile(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

    public User login(UserLoginRequest request) {
        Optional<User> opUser = this.userRepository.findByUsername(request.username());

        if (opUser.isEmpty()) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = opUser.get();

        checkPassword(request.password(), user.getPassword());

        return user;
    }

    public String genAccessToken(User user) {
        return this.authTokenService.genAccessToken(user);
    }

    public Map<String, Object> payload(String jwt){
        return authTokenService.payload(jwt);
    }

    public void checkPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }


}
