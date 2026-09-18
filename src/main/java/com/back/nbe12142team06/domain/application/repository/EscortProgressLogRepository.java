package com.back.nbe12142team06.domain.application.repository;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.entity.EscortProgressLog;
import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EscortProgressLogRepository extends JpaRepository<EscortProgressLog, Long> {
    Optional<EscortProgressLog> findByApplicationAndProgress(Application application, EscortProgress progress);
}
