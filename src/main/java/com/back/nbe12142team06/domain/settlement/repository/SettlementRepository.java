package com.back.nbe12142team06.domain.settlement.repository;

import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("select s " +
            "from Settlement s " +
            "join User u on s.escort=u " +
            "where s.id=:settlementId and (s.settlementStatus='PENDING' or s.settlementStatus='FAILED')")
    Optional<Settlement> findByIdAndState(@Param("settlementId") Long settlementId);

    // TODO: 날짜 비교 아래처럼 하지 말고 between으로 고치기
    @Query("select s " +
            "from Settlement s " +
            "join User u on s.escort=u " +
            "join fetch Post p on s.application.post=p " +
            "where u.id=:userId and p.escortStartAt >= :startDate and p.escortStartAt <= :endDate")
    Page<Settlement> findAllByUserIdAndDate(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);


    @Query("select s from Settlement s " +
            "where (s.settlementStatus='PENDING' or s.settlementStatus='FAILED') and s.settledDate <= current_date")
    List<Settlement> findAllByStatusAndDate();

    @Query("select s from Settlement s join Application a on s.application.id=a.id where a.id=:applicationId")
    Optional<Settlement> findByApplicationId(@Param("applicationId") Long applicationId);
}
