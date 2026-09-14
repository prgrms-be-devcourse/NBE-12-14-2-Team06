package com.back.nbe12142team06.domain.payment.repository;

import com.back.nbe12142team06.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select pay " +
            "from Payment pay " +
            "join Post post on pay.post=post " +
            "join User u on post.client=u " +
            "where post.id=:postId and u.id=:userId")
    Optional<Payment> findByPostIdAndUserId(@Param("postId") Long postId,
                                            @Param("userId") Long userId);
}
