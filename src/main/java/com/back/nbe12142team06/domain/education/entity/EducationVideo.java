package com.back.nbe12142team06.domain.education.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Entity
@Getter
@Check(constraints = "duration_sec > 0")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "education_video")
public class EducationVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제목
    @Column(nullable = false, length = 100)
    private String title;

    // url
    @Column(nullable = false, length = 500)
    private String url;

    // 영상 길이
    @Column(nullable = false)
    private int durationSec;

    // 필수 여부
    @Column(nullable = false)
    private boolean required;

    public EducationVideo(String title, String url, int durationSec, boolean required) {
        this.title = title;
        this.url = url;
        this.durationSec = durationSec;
        this.required = required;
    }
}
