package com.back.nbe12142team06.domain.user.repository;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscortRepository extends JpaRepository<EscortProfile, Long> {
}
