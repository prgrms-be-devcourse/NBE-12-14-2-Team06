package com.back.nbe12142team06.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseSoftDeleteTimeEntity extends BaseTimeEntity {
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
