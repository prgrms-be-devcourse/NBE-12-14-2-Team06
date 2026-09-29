package com.back.nbe12142team06.domain.user.repository;

import com.back.nbe12142team06.domain.user.entity.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, Long> {

    @Modifying
    @Query("delete from ClientProfile p where p.user.id = :userId")
    void deleteByUserId(Long userId);
}
