package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.auth.entity.RefreshToken;
import com.back.nbe12142team06.domain.auth.repository.RefreshTokenRepository;
import com.back.nbe12142team06.domain.user.dto.login.common.UserLoginRequest;
import com.back.nbe12142team06.domain.user.dto.signup.common.UserSignUpRequest;
import com.back.nbe12142team06.domain.user.dto.user.UserProfileUpdateRequest;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.BusinessException;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import com.back.nbe12142team06.global.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final RefreshTokenRepository refreshTokenRepository;

    // 회원가입
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
        // phoneNum 중복 검사
        if (this.userRepository.existsByPhoneNum(request.phoneNum())) {
            throw new DuplicatedException(DUPLICATED_PHONE_NUM, "이미 사용 중인 전화번호입니다.");
        }


        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()), // 비밀번호 암호화
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

    // 로그인
    public User login(UserLoginRequest request) {
        Optional<User> opUser = this.userRepository.findByUsername(request.username());

        if (opUser.isEmpty()) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = opUser.get();

        checkPassword(request.password(), user.getPassword());

        return user;
    }

    // 상세정보 조회
    public User myProfile(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("회원 정보를 찾을 수 없습니다. 다시 로그인해주세요."));
    }

    // username 중복 검사
    public boolean isUsernameAvailable(String username) {
        return !this.userRepository.existsByUsername(username);
    }

    // access token 생성
    public String genAccessToken(User user) {
        return this.authTokenService.genAccessToken(user);
    }

    // access token 파싱
    public Map<String, Object> payload(String jwt){
        return authTokenService.payload(jwt);
    }

    // 비밀번호 해싱값 대조
    public void checkPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    // 회원 정보 수정
    @Transactional
    public User updateMyProfile(Long id, @Valid UserProfileUpdateRequest request) {


        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("회원 정보를 찾을 수 없습니다. 다시 로그인해주세요."));

        if (this.userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicatedException(DUPLICATED_EMAIL, "이미 사용 중인 이메일입니다.");
        }
        if (this.userRepository.existsByPhoneNumAndIdNot(request.phoneNum(), id)) {
            throw new DuplicatedException(DUPLICATED_PHONE_NUM, "이미 사용 중인 전화번호입니다.");
        }

        user.updateUser(
                passwordEncoder.encode(request.password()),
                request.email(),
                request.name(),
                request.birthDate(),
                request.phoneNum(),
                request.region()
        );

        return this.userRepository.save(user);
    }

    @Transactional
    public void deleteMyProfile(Long id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("회원 정보를 찾을 수 없습니다. 다시 로그인해주세요."));

        // 해당 유저의 모든 리프레시 토큰 폐기
        List<RefreshToken> refreshTokens = this.refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(id);

        for (RefreshToken refreshToken : refreshTokens) {
            refreshToken.revoke();
        }

        user.deleteUser();

        this.userRepository.save(user);
    }

    // [관리자] 회원 정보 조회 (탈퇴한 회원 정보도 가능)
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new NotFoundException("회원 정보를 찾을 수 없습니다."));
    }
}
