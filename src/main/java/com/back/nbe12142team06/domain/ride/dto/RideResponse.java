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
        String originAddr,
        BigDecimal destLat,
        BigDecimal destLnt,
        String destAddr) {
    public RideResponse(Ride ride) {
        Post post = ride.getPost();
        BigDecimal originLat = null;
        BigDecimal originLnt = null;
        String originAddr = null;
        BigDecimal destLat = null;
        BigDecimal destLnt = null;
        String destAddr = null;
        if (ride.getDirection().equals(RideDirection.TO_HOSPITAL)) {
            originLat = post.getPickupLat();
            originLnt = post.getPickupLng();
            originAddr = post.getPickupAddress();
            destLat = post.getHospitalLat();
            destLnt = post.getHospitalLng();
            destAddr = post.getHospitalAddress();
        } else if (ride.getDirection().equals(RideDirection.TO_HOME)) {
            originLat = post.getHospitalLat();
            originLnt = post.getHospitalLng();
            originAddr = post.getHospitalAddress();
            destLat = post.getPickupLat();
            destLnt = post.getPickupLng();
            destAddr = post.getPickupAddress();
        }
        this(ride.getId(), ride.getDirection(), ride.getRideStatus(), ride.getSelected(),
                originLat, originLnt, originAddr, destLat, destLnt, destAddr);
    }
}
