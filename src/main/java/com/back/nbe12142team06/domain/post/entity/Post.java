package com.back.nbe12142team06.domain.post.entity;

import com.back.nbe12142team06.global.entity.BaseSoftDeleteTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@Table(name = "post")
@SQLDelete(sql = "UPDATE post SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")  // 삭제 시 deleted_at 세팅 (소프트 딜리트)
@SQLRestriction("deleted_at IS NULL")   // 조회 시 deleted_at IS NULL 조건 자동 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용 기본 생성자 (외부 직접 생성 불가)
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // @Builder 전용 생성자
public class Post extends BaseSoftDeleteTimeEntity {  // createdAt, updatedAt, deletedAt 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // PK (BIGINT AUTO_INCREMENT)

    @Column(name = "client_id", nullable = false)
    private Long clientId;  // 의뢰인 User ID (FK → User)

    @Column(name = "accepted_application_id")
    private Long acceptedApplicationId;  // 수락된 지원서 ID (nullable, UNIQUE)

    // ── 공고 기본 정보 ──────────────────────────
    @Column(nullable = false)
    private String title;  // 공고 제목

    @Lob
    @Column(nullable = false)
    private String content;  // 공고 상세 내용 (TEXT)

    @Column(name = "patient_note", length = 500)
    private String patientNote;  // 환자 특이사항

    @Column(nullable = false, length = 50)
    private String region;  // 지역 (예: 서울, 부산)

    // ── 병원 정보 ───────────────────────────────
    @Column(name = "hospital_name", nullable = false)
    private String hospitalName;  // 병원 이름

    @Column(name = "hospital_address", nullable = false)
    private String hospitalAddress;  // 병원 주소

    @Column(name = "hospital_lat", precision = 10, scale = 7, nullable = false)
    private BigDecimal hospitalLat;  // 병원 위도 (예: 37.5665351) - 소수점 정밀도 위해 BigDecimal

    @Column(name = "hospital_lng", precision = 10, scale = 7, nullable = false)
    private BigDecimal hospitalLng;  // 병원 경도 (예: 126.9780000)

    // ── 픽업 정보 ───────────────────────────────
    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;  // 픽업 장소 주소

    @Column(name = "pickup_lat", precision = 10, scale = 7, nullable = false)
    private BigDecimal pickupLat;  // 픽업 위도

    @Column(name = "pickup_lng", precision = 10, scale = 7, nullable = false)
    private BigDecimal pickupLng;  // 픽업 경도

    // ── 일정 및 급여 ────────────────────────────
    @Min(value = 1, message = "시급은 0보다 커야 합니다.")
    @Column(name = "hourly_pay", nullable = false)
    private int hourlyPay;  // 시급 (CHECK > 0)

    @Column(name = "escort_start_at", nullable = false)
    private LocalDateTime escortStartAt;  // 동행 시작 예정 시간

    @Column(name = "escort_end_at", nullable = false)
    private LocalDateTime escortEndAt;  // 동행 종료 예정 시간

    @Column(name = "deadline_at", nullable = false)
    private LocalDateTime deadlineAt;  // 모집 마감 시간

    // ── 상태 ────────────────────────────────────
    @Enumerated(EnumType.STRING)  // DB에 ENUM 문자열로 저장 (예: "OPEN")
    @Column(name = "post_status", nullable = false)
    @Builder.Default
    private PostStatus postStatus = PostStatus.OPEN;  // 기본값: OPEN

    @Column(name = "report_required", nullable = false)
    @Builder.Default
    private boolean reportRequired = true;  // 동행 후 보고서 작성 여부 (기본값: true)
}