package com.back.nbe12142team06.domain.user.entity;

import com.back.nbe12142team06.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "client_profiles")
public class ClientProfile extends BaseTimeEntity {

    // 회원 개인 키 ID
    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 보호자 실명
    @Column(length = 50)
    private String emergencyContactName;

    // 보호자 번호
    @Column(length = 20)
    private String emergencyContactPhone;

    // 의뢰인 특이사항
    @Column(length = 500)
    private String careNote;

    // 우선 user만 연결 생성자
    public ClientProfile(User user) {
        this.user = user;
    }
}
