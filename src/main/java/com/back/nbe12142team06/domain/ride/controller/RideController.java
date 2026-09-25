package com.back.nbe12142team06.domain.ride.controller;

import com.back.nbe12142team06.domain.ride.dto.RideResponse;
import com.back.nbe12142team06.domain.ride.dto.RideUpdateRequest;
import com.back.nbe12142team06.domain.ride.entity.Ride;
import com.back.nbe12142team06.domain.ride.service.RideService;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "이동수단", description = "동행 이동수단 조회 및 변경 관련 API")
@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

//    @Operation(
//            summary = "이동수단 변경",
//            description = "특정 이동수단 정보를 변경합니다."
//    )
//    @PutMapping("/posts/{postId}")
//    public RsData<List<RideResponse>> updateRide(@AuthenticationPrincipal SecurityUser actor,
//                                           @PathVariable Long postId,
//                                           @Valid @RequestBody RideUpdateRequest request) {
//        List<Ride> rides = rideService.updateRide(postId, request);
//
//        return new RsData<>("200-20", "이동수단이 변경되었습니다.",
//                rides.stream().map(RideResponse::new).toList());
//    }

    @Operation(
            summary = "공고별 이동수단 목록 조회",
            description = "특정 공고에 등록된 이동수단 목록을 조회합니다."
    )
    @GetMapping("/posts/{postId}")
    public RsData<List<RideResponse>> getListByPostId(@AuthenticationPrincipal SecurityUser actor,
                                                      @PathVariable Long postId) {
        Long userId = actor.getId();

        List<Ride> rides = rideService.getListByPostId(userId, postId);

        return new RsData<>("200-21", "공고글 이동 정보를 불러왔습니다.",
                rides.stream().map(RideResponse::new).toList());
    }

    @Operation(
            summary = "이동수단 상세 조회",
            description = "특정 이동수단의 상세 정보를 조회합니다."
    )
    @GetMapping("/{rideId}")
    public RsData<RideResponse> getRide(@AuthenticationPrincipal SecurityUser actor,
                                        @PathVariable Long rideId) {
        Long userId = actor.getId();

        Ride ride = rideService.findById(userId, rideId);

        return new RsData<>("200-22", "이동 정보를 불러왔습니다.",
                new RideResponse(ride));
    }


}
