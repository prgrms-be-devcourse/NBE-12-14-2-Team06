package com.back.nbe12142team06.domain.post.repository;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.client " +
            "WHERE (:keyword IS NULL OR p.title LIKE CONCAT('%', :keyword, '%') " +
            "       OR p.hospitalName LIKE CONCAT('%', :keyword, '%') " +
            "       OR p.region LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:region IS NULL OR p.region = :region) " +
            "AND (:dateFrom IS NULL OR p.escortStartAt >= :dateFrom) " +
            "AND (:dateTo IS NULL OR p.escortStartAt <= :dateTo) " +
            "AND (:minPay IS NULL OR p.hourlyPay >= :minPay) " +
            "AND (:maxPay IS NULL OR p.hourlyPay <= :maxPay) " +
            "AND ((:openOnly = TRUE AND p.postStatus = com.back.nbe12142team06.domain.post.entity.PostStatus.OPEN) " +
            "     OR (:openOnly = FALSE AND p.postStatus <> com.back.nbe12142team06.domain.post.entity.PostStatus.OPEN)) " +
            "AND EXISTS (" +
            "    SELECT 1 FROM Payment pay " +
            "    WHERE pay.post = p " +
            "      AND pay.paymentStatus = com.back.nbe12142team06.domain.payment.entity.PaymentStatus.DONE " +
            "      AND pay.id = (SELECT MAX(pay2.id) FROM Payment pay2 WHERE pay2.post = p)" +
            ")")
    Page<Post> search(@Param("keyword") String keyword,
                      @Param("region") String region,
                      @Param("dateFrom") LocalDateTime dateFrom,
                      @Param("dateTo") LocalDateTime dateTo,
                      @Param("minPay") Integer minPay,
                      @Param("maxPay") Integer maxPay,
                      @Param("openOnly") boolean openOnly,
                      Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p JOIN FETCH p.client " +
            "WHERE EXISTS (" +
            "    SELECT 1 FROM Payment pay " +
            "    WHERE pay.post = p " +
            "      AND pay.paymentStatus = com.back.nbe12142team06.domain.payment.entity.PaymentStatus.DONE " +
            "      AND pay.id = (SELECT MAX(pay2.id) FROM Payment pay2 WHERE pay2.post = p)" +
            ") " +
            "ORDER BY p.id DESC")
    Page<Post> findAllWithClient(Pageable pageable);


    @Query("SELECT p " +
            "FROM Post p JOIN FETCH p.client " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithClient(@Param("id") Long id);
    //상태코드 만료처리
    List<Post> findAllByPostStatusAndRecruitEndAtBefore(PostStatus postStatus, LocalDateTime dateTime);

    // 지원 승인(매칭) 처리 중 같은 공고에 대한 동시 승인 요청을 직렬화하기 위한 비관적 쓰기 락 조회.
    // 두 번째 요청은 첫 번째 트랜잭션이 커밋될 때까지 이 조회에서 대기하고,
    // 커밋 후에는 갱신된 PostStatus(MATCHED)를 보고 정상적인 InvalidException 을 던지게 된다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :id")
    Optional<Post> findByIdForUpdate(@Param("id") Long id);

}
