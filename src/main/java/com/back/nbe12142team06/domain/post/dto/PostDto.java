package com.back.nbe12142team06.domain.post.dto;

import com.back.nbe12142team06.domain.post.entity.Post;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//상세목록용
public record PostDto(
        Long id,
        String client_id,
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

) {
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getClient().getUsername(),
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