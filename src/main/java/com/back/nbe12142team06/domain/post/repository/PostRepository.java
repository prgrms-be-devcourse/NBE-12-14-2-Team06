package com.back.nbe12142team06.domain.post.repository;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
