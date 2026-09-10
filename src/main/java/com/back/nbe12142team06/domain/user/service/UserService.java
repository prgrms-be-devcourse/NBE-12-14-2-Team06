package com.back.nbe12142team06.domain.user.service;

import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User signUp(String username, String password, String email, String name, Role role, Gender gender, LocalDate birthDate, String phoneNumber, String region) {
        // username 중복 검사
        if (this.userRepository.existsByUsername(username)) {
            throw new DuplicatedException("이미 사용 중인 아이디입니다.");
        }

        User user = new User(username, password, email, name, role, gender, birthDate, phoneNumber, region);
        return this.userRepository.save(user);
    }
}
