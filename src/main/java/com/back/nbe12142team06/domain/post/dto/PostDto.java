package com.back.nbe12142team06.domain.post.dto;

import com.back.nbe12142team06.domain.post.entity.Post;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PostDto(
        Long id,
        String title,
        String content,
        String patientNote,
        String region,
        String hospitalName,
        String hospitalAddress,
        BigDecimal hospitalLat,
        BigDecimal hospitalLng,
        String pickupAddress,
        BigDecimal pickupLat,
        BigDecimal pickupLng,
        int hourlyPay,
        LocalDateTime escortStartAt,
        LocalDateTime escortEndAt,
        LocalDateTime deadlineAt,
        String postStatus,
        boolean reportRequired,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

        // 필요하다면 client 정보 일부만 노출
  /*      Long clientId,      // User ID만
        String clientName,  // User 이름만*/

) {
    public PostDto(Post post) {
        this(
                // Post 생성자에서
      /*          post.getClient().getId(),
                post.getClient().getName(),*/
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPatientNote(),
                post.getRegion(),
                post.getHospitalName(),
                post.getHospitalAddress(),
                post.getHospitalLat(),
                post.getHospitalLng(),
                post.getPickupAddress(),
                post.getPickupLat(),
                post.getPickupLng(),
                post.getHourlyPay(),
                post.getEscortStartAt(),
                post.getEscortEndAt(),
                post.getDeadlineAt(),
                post.getPostStatus().getDescription(),
                post.isReportRequired(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}