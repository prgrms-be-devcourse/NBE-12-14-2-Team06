package com.back.nbe12142team06.domain.application.entity;

import com.back.nbe12142team06.domain.application.enums.ApplicationStatus;
import com.back.nbe12142team06.domain.post.entity.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Application.cancelWithoutPenalty (패널티 없는 지원 취소) 단위 테스트.
 * 스프링, DB 없이 엔티티만 확인한다. (공고 취소·매칭 취소·삭제 때 지원을 정리하는 데 쓰인다.)
 */
class ApplicationCancelWithoutPenaltyTest {

    @ParameterizedTest(name = "{0} 지원을 취소하면 CANCELED 가 되고 공고 연결 값이 풀린다")
    @CsvSource({"PENDING", "ACCEPTED"})
    @DisplayName("대기중·승인 지원을 패널티 없이 취소하면 CANCELED 가 되고 acceptedPostId, activePostId 가 풀린다")
    void 지원을_취소하면_상태와_공고_연결값이_풀린다(ApplicationStatus status) {
        Post post = Post.builder().id(10L).build();
        Application application = Application.builder().post(post).build();   // 처음 상태는 PENDING
        if (status == ApplicationStatus.ACCEPTED) {
            application.accept();   // 승인되면 acceptedPostId, activePostId 가 공고 id 로 채워진다
            assertThat(application.getAcceptedPostId()).isEqualTo(10L);
            assertThat(application.getActivePostId()).isEqualTo(10L);
        }

        application.cancelWithoutPenalty();

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.CANCELED);
        assertThat(application.getAcceptedPostId()).isNull();   // 승인된 공고 연결 해제 (재매칭, 중복 방지용)
        assertThat(application.getActivePostId()).isNull();     // 진행 중인 지원이 아니므로 해제
    }
}
