package com.back.nbe12142team06.domain.ride.controller;

import com.back.nbe12142team06.domain.ride.dto.RideResponse;
import com.back.nbe12142team06.domain.ride.dto.RideUpdateRequest;
import com.back.nbe12142team06.domain.ride.entity.Ride;
import com.back.nbe12142team06.domain.ride.service.RideService;
import com.back.nbe12142team06.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    // 이동 수단 변경
    @PutMapping("/{rideId}")
    public RsData<RideResponse> updateRide(@RequestParam Long userId,
                                           @PathVariable Long rideId,
                                           @Valid @RequestBody RideUpdateRequest request) {
        Ride ride = rideService.updateRide(userId, rideId, request);

        return new RsData<>("200-20", "이동수단이 변경되었습니다.",
                new RideResponse(ride));
    }

    // 공고 별 목록
    @GetMapping("/post/{postId}")
    public RsData<List<RideResponse>> getListByPostId(@RequestParam Long userId,
                                                      @PathVariable Long postId) {
        List<Ride> rides = rideService.getListByPostId(userId, postId);

        return new RsData<>("200-21", "공고글 이동 정보를 불러왔습니다.",
                rides.stream().map(RideResponse::new).toList());
    }

    // 이동 상세 정보
    @GetMapping("/{rideId}")
    public RsData<RideResponse> getRide(@RequestParam Long userId,
                                        @PathVariable Long rideId) {
        Ride ride = rideService.findById(userId, rideId);

        return new RsData<>("200-22", "이동 정보를 불러왔습니다.",
                new RideResponse(ride));
    }


}
