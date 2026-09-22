package com.back.nbe12142team06.domain.user.repository;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EscortProfileRepository extends JpaRepository<EscortProfile, Long> {

    @Modifying
    @Query("delete from EscortProfile p where p.user.id = :userId")
    void deleteByUserId(Long userId);

}
