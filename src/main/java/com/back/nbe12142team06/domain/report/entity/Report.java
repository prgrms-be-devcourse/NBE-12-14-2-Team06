package com.back.nbe12142team06.domain.report.entity;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@Table(name = "report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용 기본 생성자 (외부 직접 생성 불가)
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // @Builder 전용 생성자
public class Report extends BaseTimeEntity {  // createdAt, updatedAt 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // PK (BIGINT AUTO_INCREMENT)

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;  // 지원서 (FK → Application, UNIQUE / 동행 1건당 보고서 1개)

    @Column(nullable = false, length = 100)
    private String title;  // 보고서 제목

    @Lob
    @Column(name = "origin_content", nullable = false)
    private String originContent;  // 동행인 작성 원문 (TEXT)

    @Lob
    @Column(name = "ai_summary")
    private String aiSummary;  // LLM 요약본 (TEXT, nullable / 요약 완료 후 채워짐)

    @Column(name = "summarized_at")
    private LocalDateTime summarizedAt;  // AI 요약 완료 시각 (nullable)
}