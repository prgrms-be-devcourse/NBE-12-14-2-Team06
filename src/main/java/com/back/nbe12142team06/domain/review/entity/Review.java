package com.back.nbe12142team06.domain.review.entity;

import com.back.nbe12142team06.domain.application.entity.Application;
import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Builder
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용 기본 생성자 (외부 직접 생성 불가)
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // @Builder 전용 생성자
public class Review extends BaseTimeEntity {  // createdAt, updatedAt 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // PK (BIGINT AUTO_INCREMENT)

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;  // 지원서 (FK → Application, UNIQUE / 동행 1건당 리뷰 1개)

    @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int rating;  // 별점 (CHECK 1~5)

    // 선택형 후기 태그 (다중 선택 최대 5개 / review_tag 테이블에 별도 저장)
    @Builder.Default
    @Enumerated(EnumType.STRING)  // DB에 ENUM 문자열로 저장 (예: "KIND")
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "review_tag",
            joinColumns = @JoinColumn(name = "review_id")
    )
    @Column(name = "tag", length = 30, nullable = false)
    private Set<ReviewTag> tags = new HashSet<>();

    @Column(length = 500)
    private String content;  // 리뷰 내용 (nullable, 선택 입력)
}