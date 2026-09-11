package com.back.nbe12142team06.domain.ride.repository;

import com.back.nbe12142team06.domain.ride.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, Long> {
}
