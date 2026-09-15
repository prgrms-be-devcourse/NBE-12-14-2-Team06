package com.back.nbe12142team06.domain.ride.dto;

import com.back.nbe12142team06.domain.ride.entity.Ride;
import com.back.nbe12142team06.domain.ride.entity.RideDirection;
import com.back.nbe12142team06.domain.ride.entity.RideSelect;
import com.back.nbe12142team06.domain.ride.entity.RideStatus;

public record RideResponse(Long id, RideDirection direction, RideStatus status, RideSelect selected) {
    public RideResponse(Ride ride) {
        this(ride.getId(), ride.getDirection(), ride.getRideStatus(), ride.getSelected());
    }
}
