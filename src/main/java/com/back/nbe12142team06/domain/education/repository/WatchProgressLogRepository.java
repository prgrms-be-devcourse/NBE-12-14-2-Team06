package com.back.nbe12142team06.domain.education.repository;

import com.back.nbe12142team06.domain.education.entity.EducationVideo;
import com.back.nbe12142team06.domain.education.entity.WatchProgressLog;
import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchProgressLogRepository extends JpaRepository<WatchProgressLog, Long> {

    List<WatchProgressLog> findByEscortProfileAndEducationVideoOrderByReceivedAtAscIdAsc(EscortProfile escortProfile, EducationVideo educationVideo);
}
