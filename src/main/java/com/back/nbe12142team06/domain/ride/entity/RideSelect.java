package com.back.nbe12142team06.domain.ride.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RideSelect {
    WALK("도보"),
    BUS("버스"),
    TAXI("택시"),
    OWN_CAR("자차");

    private final String description;
}
