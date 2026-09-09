package com.back.nbe12142team06.domain.application.entity;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Application extends BaseTimeEntity {
    // 지원 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 지원한 공고
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 지원한 동행인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escort_id", nullable = false)
    private User escort;

    // 지원 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    // 최종 승인된 공고 ID
    @Column(name = "accepted_post_id", unique = true)
    private Long acceptedPostId;
}
