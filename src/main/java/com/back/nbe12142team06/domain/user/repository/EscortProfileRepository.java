package com.back.nbe12142team06.domain.user.repository;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EscortProfileRepository extends JpaRepository<EscortProfile, Long> {

    @Modifying
    @Query("delete from EscortProfile p where p.user.id = :userId")
    void deleteByUserId(Long userId);

    // user 를 함께 가져옵니다. EscortProfileResponse 등 응답 DTO 가 escortProfile.getUser() 를
    // 쓰는데, user 가 지연 로딩(LAZY)이라 트랜잭션 밖(컨트롤러)에서 접근하면
    // LazyInitializationException 이 나서 fetch join 으로 가져옵니다.
    @Query("select p from EscortProfile p join fetch p.user where p.userId = :userId")
    Optional<EscortProfile> findByIdWithUser(@Param("userId") Long userId);

}
