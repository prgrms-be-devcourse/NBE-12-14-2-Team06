package com.back.nbe12142team06.domain.user.repository;

import com.back.nbe12142team06.domain.user.entity.EscortProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EscortProfileRepository extends JpaRepository<EscortProfile, Long> {

    @Query("select p from EscortProfile p join fetch p.user where p.userId = :userId")
    Optional<EscortProfile> findByIdWithUser(@Param("userId") Long userId);


    @Modifying
    @Query("delete from EscortProfile p where p.user.id = :userId")
    void deleteByUserId(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update EscortProfile e
        set e.verified = true, e.verifiedAt = :now, e.version = e.version + 1
        where e.userId = :userId and e.verified = false
        """)
    int verify(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update EscortProfile e
        set e.ratingSum = e.ratingSum + :rating, e.ratingCount = e.ratingCount + 1,
            e.version = e.version + 1
        where e.userId = :userId
        """)
    int addRating(@Param("userId") Long userId, @Param("rating") int rating);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update EscortProfile e
        set e.completedCount = e.completedCount + 1, e.version = e.version + 1
        where e.userId = :userId
        """)
    int increaseCompletedCount(@Param("userId") Long userId);

}
