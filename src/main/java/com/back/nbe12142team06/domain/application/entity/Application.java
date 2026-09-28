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
// (post_id, escort_id) 나 (post_id, escort_id, status) 로 unique 를 걸면
// 취소 후 재지원·재취소가 막힌다. 끝난 지원(CANCELED 등) 행이 여러 건 쌓이면
// status 까지 포함한 조합끼리 서로 충돌하기 때문이다 (예: 취소된 행이 두 번째
// 생기는 순간 첫 번째 취소 행과 (post_id, escort_id, CANCELED) 가 겹친다).
// 대신 activePostId 컬럼으로 "진행 중인 지원"만 가려내 그 값에만 unique 를 건다.
@Table(name = "application", uniqueConstraints = @UniqueConstraint(columnNames = {"active_post_id", "escort_id"}))
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

    // 최종 승인된 공고 ID. 상태가 ACCEPTED 일 때만 post 의 id, 아니면 null 이다.
    // (한 공고에 승인된 지원은 하나뿐이라는 것을 이 unique 제약으로 보장한다.)
    @Column(name = "accepted_post_id", unique = true)
    private Long acceptedPostId;

    // 진행 중인 지원만 가리키는 값. 상태가 PENDING 또는 ACCEPTED 이면 post 의 id,
    // 그 외(CANCELED/REJECTED/NO_SHOW)면 null 이다.
    // acceptedPostId 와 같은 트릭이지만 목적이 다르다 — acceptedPostId 는 "승인된
    // 지원은 공고당 하나" 를, activePostId 는 "동행인 한 명이 한 공고에 동시에
    // 가질 수 있는 진행 중인 지원은 하나" 를 보장한다. DB 의 unique 인덱스는 NULL 을
    // 서로 다른 값으로 취급하므로, 끝난 지원은 activePostId 가 전부 null 이 되어
    // 몇 건이 쌓여도 서로 충돌하지 않는다.
    @Column(name = "active_post_id")
    private Long activePostId;

    // 상태 전이마다 activePostId 를 다시 계산한다. 호출부에서 값을 직접 챙기지
    // 않아도 항상 상태와 일치하도록, 새로 저장될 때(@PrePersist)와 상태를 바꾸는
    // 메서드 안에서 한 곳(이 메서드)만 거치게 한다.
    private void syncActivePostId() {
        this.activePostId = (status == ApplicationStatus.PENDING || status == ApplicationStatus.ACCEPTED)
                ? this.post.getId()
                : null;
    }

    @PrePersist
    private void onCreate() {
        syncActivePostId();
    }

    public void accept() {
        this.status = ApplicationStatus.ACCEPTED;
        this.acceptedPostId = this.post.getId();
        syncActivePostId();
    }

    public void reject() {
        this.status = ApplicationStatus.REJECTED;
        syncActivePostId();
    }

    public void cancel() {
        this.status = ApplicationStatus.CANCELED;
        syncActivePostId();
    }

    public void noShow() {
        this.status = ApplicationStatus.NO_SHOW;
        // 재매칭을 위해 승인된 공고 ID 초기화
        this.acceptedPostId = null;
        syncActivePostId();
    }
}
