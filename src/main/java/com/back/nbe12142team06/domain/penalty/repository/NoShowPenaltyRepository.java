package com.back.nbe12142team06.domain.penalty.repository;

import com.back.nbe12142team06.domain.penalty.entity.NoShowPenalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoShowPenaltyRepository extends JpaRepository<NoShowPenalty, Long> {

    @Query("select p from NoShowPenalty p " +
            "where p.application.escort.id=:escortId and p.status = 'PENDING' " +
            "order by p.id")
    List<NoShowPenalty> findByEscortIdAndStatus(@Param("escortId") Long escortId);
}
