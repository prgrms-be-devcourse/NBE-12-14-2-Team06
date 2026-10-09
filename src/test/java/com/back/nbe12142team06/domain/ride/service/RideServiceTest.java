package com.back.nbe12142team06.domain.ride.service;

import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.ride.entity.Ride;
import com.back.nbe12142team06.domain.ride.entity.RideDirection;
import com.back.nbe12142team06.domain.ride.entity.RideStatus;
import com.back.nbe12142team06.domain.ride.repository.RideRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class RideServiceTest {

    @Autowired
    private RideService rideService;
    @MockitoSpyBean
    private RideRepository rideRepository;

    private Ride createRide(Long id, RideDirection direction, RideStatus status, Post post) {
        return Ride.builder()
                .id(id)
                .direction(direction)
                .rideStatus(status)
                .post(post)
                .build();
    }

    @Test
    @DisplayName("[RideService] ACCEPTED → IN_PROGRESS, 병원 이동")
    void hospitalRideStatusAcceptedToInProgress() {
        Ride rideToHospital = createRide(1L, RideDirection.TO_HOSPITAL, RideStatus.ACCEPTED, null);
        Ride rideToHome = createRide(2L, RideDirection.TO_HOME, RideStatus.ACCEPTED, null);

        doReturn(List.of(rideToHospital, rideToHome))
                .when(rideRepository).findByPostId(any());

        List<Ride> rides = rideService.move(1L, EscortProgress.TO_HOSPITAL);
        Ride updatedRide = null;
        for (Ride ride : rides) {
            if (ride.getDirection().equals(RideDirection.TO_HOSPITAL)) {
                updatedRide = ride;
            }
        }

        assertThat(updatedRide.getRideStatus()).isEqualTo(RideStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("[RideService] ACCEPTED → IN_PROGRESS, 집 이동")
    void homeRideStatusAcceptedToInProgress() {
        Ride rideToHospital = createRide(1L, RideDirection.TO_HOSPITAL, RideStatus.COMPLETED, null);
        Ride rideToHome = createRide(2L, RideDirection.TO_HOME, RideStatus.ACCEPTED, null);

        doReturn(List.of(rideToHospital, rideToHome))
                .when(rideRepository).findByPostId(any());

        List<Ride> rides = rideService.move(1L, EscortProgress.TO_HOME);
        Ride updatedRide = null;
        for (Ride ride : rides) {
            if (ride.getDirection().equals(RideDirection.TO_HOME)) {
                updatedRide = ride;
            }
        }

        assertThat(updatedRide.getRideStatus()).isEqualTo(RideStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("[RideService] IN_PROGRESS → COMPLETED, 병원 이동")
    void hospitalRideStatusInProgressToCompleted() {
        Ride rideToHospital = createRide(1L, RideDirection.TO_HOSPITAL, RideStatus.IN_PROGRESS, null);
        Ride rideToHome = createRide(2L, RideDirection.TO_HOME, RideStatus.ACCEPTED, null);

        doReturn(List.of(rideToHospital, rideToHome))
                .when(rideRepository).findByPostId(any());

        List<Ride> rides = rideService.move(1L, EscortProgress.AT_HOSPITAL);
        Ride updatedRide = null;
        for (Ride ride : rides) {
            if (ride.getDirection().equals(RideDirection.TO_HOSPITAL)) {
                updatedRide = ride;
            }
        }

        assertThat(updatedRide.getRideStatus()).isEqualTo(RideStatus.COMPLETED);
    }

    @Test
    @DisplayName("[RideService] IN_PROGRESS → COMPLETED, 집 이동")
    void homeRideStatusInProgressToCompleted() {
        Ride rideToHospital = createRide(1L, RideDirection.TO_HOSPITAL, RideStatus.COMPLETED, null);
        Ride rideToHome = createRide(2L, RideDirection.TO_HOME, RideStatus.IN_PROGRESS, null);

        doReturn(List.of(rideToHospital, rideToHome))
                .when(rideRepository).findByPostId(any());

        List<Ride> rides = rideService.move(1L, EscortProgress.ARRIVED_HOME);
        Ride updatedRide = null;
        for (Ride ride : rides) {
            if (ride.getDirection().equals(RideDirection.TO_HOSPITAL)) {
                updatedRide = ride;
            }
        }

        assertThat(updatedRide.getRideStatus()).isEqualTo(RideStatus.COMPLETED);
    }

    @Test
    @DisplayName("[RideService] COMPLETED에서 move")
    void rideStatusCompletedMove() {
        Ride rideToHospital = createRide(1L, RideDirection.TO_HOSPITAL, RideStatus.COMPLETED, null);
        Ride rideToHome = createRide(2L, RideDirection.TO_HOME, RideStatus.COMPLETED, null);

        doReturn(List.of(rideToHospital, rideToHome))
                .when(rideRepository).findByPostId(any());

        List<Ride> rides = rideService.move(1L, EscortProgress.ARRIVED_HOME);

        assertThat(rides.get(0).getRideStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(rides.get(1).getRideStatus()).isEqualTo(RideStatus.COMPLETED);
    }
}
