package com.back.nbe12142team06.domain.settlement.repository;

import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
