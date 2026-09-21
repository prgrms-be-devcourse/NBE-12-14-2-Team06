package com.back.nbe12142team06.domain.user.repository;


import com.back.nbe12142team06.domain.settlement.dto.AccountDto;
import com.back.nbe12142team06.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // username으로 튜플 검색
    Optional<User> findByUsername(String username);

    // DB에 해당 username이 존재하는지 확인
    boolean existsByUsername(String username);

    // DB에 해당 email이 존재하는지 확인
    boolean existsByEmail(String email);

    // DB에 해당 전화번호가 존재하는지 확인
    boolean existsByPhoneNum(String phoneNum);

    // 회원정보 수정 시 이메일 중복 체크용
    boolean existsByEmailAndIdNot(String email, Long id);

    // 회원정보 수정 시 전화번호 중복 체크용
    boolean existsByPhoneNumAndIdNot(String phoneNum, Long id);

    // [ADMIN] 탈퇴 회원 포함 단건 조회
    @Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
    Optional<User> findByIdIncludingDeleted(@Param("id") Long id);

    // [ADMIN] 탈퇴 회원 포함 목록 조회
    @Query(
            value = "SELECT * FROM users order by id DESC",
            countQuery = "SELECT COUNT(*) FROM users",
            nativeQuery = true
    )
    Page<User> findAllIncludingDeleted(Pageable pageable);
}

