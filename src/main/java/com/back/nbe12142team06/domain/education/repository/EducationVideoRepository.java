package com.back.nbe12142team06.domain.education.repository;

import com.back.nbe12142team06.domain.education.entity.EducationVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationVideoRepository extends JpaRepository<EducationVideo, Long> {
    List<EducationVideo> findAllByRequiredTrue();

    long countByRequiredTrue();
}
