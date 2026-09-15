package com.back.nbe12142team06.domain.report.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.report.dto.ReportWriteRequest;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.report.repository.ReportRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용 트랜잭션으로 설정
public class ReportService {

    private final ReportRepository reportRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional // 쓰기 작업을 수행하는 메서드에는 readOnly 해제
    public Report write(Long applicationId, ReportWriteRequest request) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(1, "존재하지 않는 동행 건입니다."));

        if (reportRepository.existsByApplicationId(applicationId)) {
            throw new DuplicatedException(1, "이미 보고서가 작성된 동행 건입니다.");
        }

        return reportRepository.save(
                Report.builder()
                        .application(application)
                        .title(request.title())
                        .originContent(request.originContent())
                        .build()
        );
    }


// 진료 보고서 조회 (클래스의 readOnly 적용)

    public Report findByApplicationId(Long applicationId) {

        if (!applicationRepository.existsById(applicationId)) {
            throw new NotFoundException(1, "존재하지 않는 동행 건입니다.");
        }

        return reportRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException(2, "작성된 보고서가 없습니다."));
    }
}
