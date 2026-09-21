package com.back.nbe12142team06.domain.settlement.repository;

import com.back.nbe12142team06.domain.settlement.dto.AccountDto;
import com.back.nbe12142team06.domain.settlement.entity.Settlement;
import com.back.nbe12142team06.domain.settlement.entity.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("select s " +
            "from Settlement s " +
            "join fetch User u on s.escort=u " +
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


    @Query("select s.id, ep.accountNumber, e.name, s.payoutAmount " +
            "from Settlement s " +
            "join s.escort e " +
            "join EscortProfile ep on ep.userId=e.id " +
            "where (s.settlementStatus='PENDING' or s.settlementStatus='FAILED') and s.settledDate <= current_date")
    List<AccountDto> findAllByStatusAndDate();

    // clearAutomatically는 1차 캐시를 비워줌 -> 테스트에서 검증할 때 status 반영이 안되서 추가
    @Modifying(clearAutomatically = true)
    @Query("update Settlement s set s.settlementStatus=:status where s.id=:id")
    int updateStatus(@Param("id") Long id, @Param("status") SettlementStatus status);

    @Query("select ep.accountNumber from Settlement s join s.escort e join EscortProfile ep on ep.userId=e.id where e.id=:userId")
    String findAccountByUserId(@Param("userId") Long userId);
}
