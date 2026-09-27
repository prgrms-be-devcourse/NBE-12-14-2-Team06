package com.back.nbe12142team06.domain.application.entity;

import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
// (post_id, escort_id) 만 unique 로 걸면 취소 후 재지원이 막힌다.
// status 를 포함해야 "같은 공고에 같은 동행인의 진행 중인 지원은 하나뿐" 이라는
// 정책만 강제하면서, CANCELED 로 끝난 지원과는 별개로 재지원(새 PENDING 행 생성)이 가능하다.
@Table(name = "application", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "escort_id", "status"}))
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
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    // 최종 승인된 공고 ID
    @Column(name = "accepted_post_id", unique = true)
    private Long acceptedPostId;

    public void accept() {
        this.status = ApplicationStatus.ACCEPTED;
        this.acceptedPostId = this.post.getId();
    }

    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }

    public void cancel() {
        this.status = ApplicationStatus.CANCELED;
    }

    public void noShow() {
        this.status = ApplicationStatus.NO_SHOW;
        // 재매칭을 위해 승인된 공고 ID 초기화
        this.acceptedPostId = null;
    }
}
