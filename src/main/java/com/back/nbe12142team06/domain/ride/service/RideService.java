package com.back.nbe12142team06.domain.ride.service;

import com.back.nbe12142team06.domain.application.enums.EscortProgress;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.ride.dto.RideResponse;
import com.back.nbe12142team06.domain.ride.dto.RideUpdateRequest;
import com.back.nbe12142team06.domain.ride.entity.Ride;
import com.back.nbe12142team06.domain.ride.entity.RideDirection;
import com.back.nbe12142team06.domain.ride.entity.RideSelect;
import com.back.nbe12142team06.domain.ride.entity.RideStatus;
import com.back.nbe12142team06.domain.ride.repository.RideRepository;
import com.back.nbe12142team06.global.exception.ForbiddenException;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private final RideRepository rideRepository;

    // 이동 수단 변경
    @Transactional
    public List<Ride> updateRide(Long postId, RideUpdateRequest request) {
        List<Ride> rides = findByPostId(postId);

        for (Ride ride : rides) {
            RideStatus status = ride.getRideStatus();
            if (status.equals(RideStatus.IN_PROGRESS) ||
                    status.equals(RideStatus.COMPLETED)) {
                throw new InvalidException(81, "이미 이동 중이거나 이동 완료이므로 수정이 불가능합니다.");
            }

            if (ride.getDirection().equals(RideDirection.TO_HOSPITAL)) {
                ride.rideUpdate(request.rideSelectToHospital().toString());
            } else {
                ride.rideUpdate(request.rideSelectToHome().toString());
            }
        }

        return rides;
    }

    // 공고로 상세 정보 찾기
    public List<Ride> findByPostId(Long postId) {
        List<Ride> rides = rideRepository.findByPostId(postId);
        if (rides.isEmpty()) {
            throw new NotFoundException(81, "해당 공고의 이동 정보를 찾을 수 없습니다.");
        }
        return rides;
    }

    // 공고로 목록 찾기
    public List<RideResponse> getListByPostId(Long postId) {
        return rideRepository.findAllByPostId(postId)
                .stream()
                .map(RideResponse::new)
                .toList();
    }

    // 유저 검증
    private boolean validUser(Ride ride, Long userId) {
        return ride.getPost().getClient().getId().equals(userId);
    }

    // 상세 정보 찾기
    public Ride findById(Long userId, Long rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(() ->
                new NotFoundException(82, "이동 정보를 찾을 수 없습니다."));

        if (!validUser(ride, userId)) {
            throw new ForbiddenException(81, "이동 정보를 조회할 권한이 없습니다.");
        }

        return ride;
    }

    @Transactional
    public List<Ride> move(Long postId, EscortProgress progress) {
        List<Ride> rides = rideRepository.findByPostId(postId);

        for (Ride ride : rides) {
            if (ride.getRideStatus().equals(RideStatus.COMPLETED)) continue;

            if (ride.getDirection().equals(RideDirection.TO_HOSPITAL)) {
                if (progress.equals(EscortProgress.TO_HOSPITAL)) {
                    ride.updateStatus(RideStatus.IN_PROGRESS);
                } else if(progress.equals(EscortProgress.AT_HOSPITAL)) {
                    ride.updateStatus(RideStatus.COMPLETED);
                }
            } else if (ride.getDirection().equals(RideDirection.TO_HOME)) {
                if (progress.equals(EscortProgress.TO_HOME)) {
                    ride.updateStatus(RideStatus.IN_PROGRESS);
                } else if (progress.equals(EscortProgress.ARRIVED_HOME)) {
                    ride.updateStatus(RideStatus.COMPLETED);
                }
            }
        }

        return rides;
    }

    // 이동 완료 (관리자 전용으로 돌릴 수 있음)
    @Transactional
    public Ride arrive(Long userId, Long rideId) {
        Ride ride = findById(userId, rideId);

        ride.updateStatus(RideStatus.COMPLETED);

        return ride;
    }

    @Transactional
    public void createRide(Post post, RideSelect toHospital, RideSelect toHome) {
        Ride rideToHos = Ride.builder()
                .post(post)
                .selected(toHospital)
                .build();
        Ride rideToHome = Ride.builder()
                .post(post)
                .selected(toHome)
                .direction(RideDirection.TO_HOME)
                .build();
        rideRepository.save(rideToHos);
        rideRepository.save(rideToHome);
    }
}
