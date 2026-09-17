package com.back.nbe12142team06.domain.report.service;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.domain.application.repository.ApplicationRepository;
import com.back.nbe12142team06.domain.report.dto.ReportWriteRequest;
import com.back.nbe12142team06.domain.report.entity.Report;
import com.back.nbe12142team06.domain.report.repository.ReportRepository;
import com.back.nbe12142team06.global.exception.DuplicatedException;
import com.back.nbe12142team06.global.exception.ForbiddenException;
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
    public Report write(Long applicationId, Long actorId, ReportWriteRequest request) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(1, "존재하지 않는 동행 건입니다."));

        // 보고서는 해당 동행 건을 수행한 동행인만 작성 가능
        if (!application.getEscort().getId().equals(actorId)) {
            throw new ForbiddenException(1, "본인이 수행한 동행 건에만 보고서를 작성할 수 있습니다.");
        }

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
    public Report findByApplicationId(Long applicationId, Long actorId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(1, "존재하지 않는 동행 건입니다."));

        // 진료 내용은 민감정보이므로 해당 동행 건의 의뢰인과 동행인만 조회 가능
        Long clientId = application.getPost().getClient().getId();
        Long escortId = application.getEscort().getId();

        if (!actorId.equals(clientId) && !actorId.equals(escortId)) {
            throw new ForbiddenException(2, "본인의 동행 건만 조회할 수 있습니다.");
        }

        return reportRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException(2, "작성된 보고서가 없습니다."));
    }
}