package com.back.nbe12142team06.domain.user.repository;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EscortProfileRepository extends JpaRepository<EscortProfile, Long> {

    @Modifying
    @Query("delete from EscortProfile p where p.user.id = :userId")
    void deleteByUserId(Long userId);

    // user 를 함께 가져옵니다. EscortProfileResponse 등 응답 DTO 가 escortProfile.getUser() 를
    // 쓰는데, user 가 지연 로딩(LAZY)이라 트랜잭션 밖(컨트롤러)에서 접근하면
    // LazyInitializationException 이 나서 fetch join 으로 가져옵니다.
    @Query("select p from EscortProfile p join fetch p.user where p.userId = :userId")
    Optional<EscortProfile> findByIdWithUser(@Param("userId") Long userId);

    // 엔티티를 읽어 필드를 더티체킹으로 갱신하면, 같은 동행인에게 리뷰가 동시에 여러 건
    // 작성될 때 두 트랜잭션이 같은 초기값을 읽고 각자 절대값으로 덮어써 lost update 가
    // 발생한다. DB 에 "현재 값 + 증가분" 을 맡기는 원자적 UPDATE 로 바꿔 이를 막는다.
    //
    // @Version 낙관적 락은 쓰지 않는다. 낙관적 락은 충돌이 감지되면
    // ObjectOptimisticLockingFailureException 을 던질 뿐이고, 그 후 다시 읽어서
    // 재시도하는 로직은 호출자가 별도로 구현해야 한다. 지금 ReviewService/PostService
    // 호출부에는 그런 재시도 로직이 없어서, 낙관적 락만 추가하면 "유실 없이 반영"이
    // 아니라 "동시 요청 중 하나가 예외로 실패"하는 것으로 문제가 바뀔 뿐이다.
    // 반면 이 값들은 순서에 상관없이 더해지기만 하면 되므로, 재시도 없이도 정합성을
    // 보장하는 원자적 UPDATE 가 더 단순하고 적합하다.
    //
    // flushAutomatically 로 먼저 영속성 컨텍스트의 대기 중인 변경(예: PostService.escortComplete()
    // 에서 이 메서드 호출 직전에 post.complete() 로 더티체킹 대상이 된 Post)을 DB 에 반영한
    // 뒤에, clearAutomatically 로 컨텍스트를 비운다. 순서를 이렇게 두면 벌크 UPDATE 이후
    // 같은 트랜잭션에서 EscortProfile 을 다시 조회해도(findById 등) 1차 캐시의 오래된
    // 값이 아니라 방금 반영된 값을 새로 읽어오면서도, 먼저 flush 된 Post 등 다른 엔티티의
    // 변경 사항은 유실되지 않는다.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update EscortProfile e set e.ratingSum = e.ratingSum + :rating, " +
            "e.ratingCount = e.ratingCount + 1 where e.userId = :userId")
    void addRating(@Param("userId") Long userId, @Param("rating") int rating);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update EscortProfile e set e.completedCount = e.completedCount + 1 " +
            "where e.userId = :userId")
    void increaseCompletedCount(@Param("userId") Long userId);

}
