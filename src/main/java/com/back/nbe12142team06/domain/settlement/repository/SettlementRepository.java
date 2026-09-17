package com.back.nbe12142team06.domain.settlement.repository;

import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("select s " +
            "from Settlement s " +
            "join User u on s.escort=u " +
            "where u.id=:userId and s.id=:settlementId and (s.settlementStatus='PENDING' or s.settlementStatus='FAILED')")
    Optional<Settlement> findByIdAndState(@Param("settlementId") Long settlementId);

    @Query("select s " +
            "from Settlement s " +
            "join User u on s.escort=u " +
            "join fetch Post p on s.application.post=p " +
            "where u.id=:userId and p.escortStartAt >= :startDate and p.escortStartAt <= :endDate")
    List<Settlement> findAllByUserIdAndDate(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);


}
