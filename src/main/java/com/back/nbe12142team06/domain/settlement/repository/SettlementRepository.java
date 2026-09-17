package com.back.nbe12142team06.domain.settlement.repository;

import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("select s " +
            "from Settlement s " +
            "join s.escort e on e.id=:userId " +
            "where s.id=:settlementId and (s.settlementStatus='PENDING' or s.settlementStatus='FAILED')")
    Optional<Settlement> findByIdAndUserId(@Param("settlementId") Long settlementId,
                                           @Param("userId") Long userId);
}
