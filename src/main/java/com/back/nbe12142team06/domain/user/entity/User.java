package com.back.nbe12142team06.domain.user.entity;

import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.global.entity.BaseSoftDeleteTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseSoftDeleteTimeEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디
    @Column(nullable = false, unique = true)
    private String username;

    // 로그인 비밀번호
    @Column(nullable = false)
    private String password;

    // 회원 이메일
    @Column(nullable = false, unique = true)
    private String email;

    // 회원 실명
    @Column(nullable = false, length = 50)
    private String name;

    // 회원 역할
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // 회원 성별
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    // 회원 생년월일
    @Column(nullable = false)
    private LocalDate birthDate;

    // 회원 전화번호
    @Column(nullable = false, length = 20)
    private String phoneNum;

    // 지역
    @Column(nullable = false, length = 50)
    private String region;
}
