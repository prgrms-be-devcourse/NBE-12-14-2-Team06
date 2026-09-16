package com.back.nbe12142team06.domain.ride.repository;

import com.back.nbe12142team06.domain.ride.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    @Query("select r from Ride r join Post p on r.post=p where p.id=:postId")
    List<Ride> findByPostId(@Param("postId") Long postId);

    @Query("select r from Ride r join Post p on r.post=p where p.id=:postId")
    List<Ride> findAllByPostId(Long postId);
}
