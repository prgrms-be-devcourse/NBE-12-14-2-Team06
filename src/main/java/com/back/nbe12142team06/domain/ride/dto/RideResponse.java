package com.back.nbe12142team06.domain.ride.dto;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.ride.entity.Ride;
import com.back.nbe12142team06.domain.ride.entity.RideDirection;
import com.back.nbe12142team06.domain.ride.entity.RideSelect;
import com.back.nbe12142team06.domain.ride.entity.RideStatus;

import java.math.BigDecimal;

public record RideResponse(
        Long id,
        RideDirection direction,
        RideStatus status,
        RideSelect selected,
        BigDecimal originLat,
        BigDecimal originLnt,
        BigDecimal destLat,
        BigDecimal destLnt) {
    public RideResponse(Ride ride) {
        Post post = ride.getPost();
        BigDecimal originLat = null;
        BigDecimal originLnt = null;
        BigDecimal destLat = null;
        BigDecimal destLnt = null;
        if (ride.getDirection().equals(RideDirection.TO_HOSPITAL)) {
            originLat = post.getPickupLat();
            originLnt = post.getPickupLng();
            destLat = post.getHospitalLat();
            destLnt = post.getHospitalLng();
        } else if (ride.getDirection().equals(RideDirection.TO_HOME)) {
            originLat = post.getHospitalLat();
            originLnt = post.getHospitalLng();
            destLat = post.getPickupLat();
            destLnt = post.getPickupLng();
        }
        this(ride.getId(), ride.getDirection(), ride.getRideStatus(), ride.getSelected(),
                originLat, originLnt, destLat, destLnt);
    }
}
