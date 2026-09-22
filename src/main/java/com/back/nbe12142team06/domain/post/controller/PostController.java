package com.back.nbe12142team06.domain.post.controller;

import com.back.nbe12142team06.domain.post.dto.PostDto;
import com.back.nbe12142team06.domain.post.dto.PostSearchConditionDto;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.dto.PostWriteResponse;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "공고", description = "동행 공고 등록, 조회, 수정, 삭제 및 상태 변경 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    @Operation(
            summary = "공고 목록 조회",
            description = "검색 조건과 정렬 조건을 적용하여 공고 목록을 페이지 단위로 조회합니다."
    )
    @GetMapping
    public RsData<Page<PostDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Integer minPay,
            @RequestParam(required = false) Integer maxPay,
            @RequestParam(defaultValue = "latest") String sort,
            // 모집중 탭(기본값) / 마감 탭 — 신규·모집중 공고가 마감 공고에 밀리지 않도록 목록 자체를 분리
            @RequestParam(defaultValue = "true") boolean openOnly) {

        Sort sortOption = switch (sort) {
            case "payHigh" -> Sort.by(Sort.Direction.DESC, "hourlyPay");
            case "payLow" -> Sort.by(Sort.Direction.ASC, "hourlyPay");
            default -> Sort.by(Sort.Direction.DESC, "id"); // 최신순 (기본값)
        };

        PostSearchConditionDto condition = new PostSearchConditionDto(
                keyword, region, dateFrom, dateTo, minPay, maxPay, openOnly);

        Page<PostDto> postDtoPage = postService.search(condition, PageRequest.of(page, size, sortOption))
                .map(PostDto::new);

        if (postDtoPage.isEmpty()) {
            return new RsData<>("200-2", "조회 내역이 없습니다.", postDtoPage);
        }
        return new RsData<>("200-1", "목록 조회 성공", postDtoPage);
    }

    @Operation(
            summary = "공고 상세 조회",
            description = "특정 공고의 상세 정보를 조회합니다."
    )
    @GetMapping("/{postId}")
    public RsData<PostDto> detail(@PathVariable Long postId) {
        PostDto postDto = new PostDto(postService.findById(postId));

        return new RsData<>("200-1", "상세 조회 성공", postDto);
    }

    @Operation(
            summary = "공고 등록",
            description = "의뢰인 또는 관리자가 새로운 동행 공고를 등록합니다."
    )
    @PostMapping
    public RsData<PostWriteResponse> write(
            @AuthenticationPrincipal SecurityUser actor,
            @RequestBody @Valid PostWriteRequest request) {

        PostWriteResponse response = postService.write(actor.getId(), request);
        return new RsData<>(
                "201-1",
                "%d번 글이 성공적으로 등록되었습니다".formatted(response.id()),
                response
        );
    }

    @Operation(
            summary = "공고 수정",
            description = "의뢰인은 본인이 작성한 모집 중 공고를 모집 시작 전에 수정할 수 있으며, 관리자도 공고를 수정할 수 있습니다."
    )
    @PutMapping("/{postId}")
    public RsData<PostDto> modify(
            @AuthenticationPrincipal SecurityUser actor,
            @PathVariable Long postId,
            @RequestBody @Valid PostWriteRequest request
    ) {
        postService.modify(postId, actor.getId(), request);

        return new RsData<>(
                "200-1",
                "%d번 게시물이 수정되었습니다.".formatted(postId)
        );
    }

    @Operation(
            summary = "공고 삭제",
            description = "의뢰인은 본인이 작성한 모집 중 또는 만료된 공고를 삭제할 수 있으며, 관리자도 공고를 삭제할 수 있습니다."
    )
    @DeleteMapping("/{postId}")
    public RsData<PostDto> delete(
            @AuthenticationPrincipal SecurityUser actor,
            @PathVariable Long postId) {

        postService.delete(postId,actor.getId());

        return new RsData<>(
                "200-1",
                "%d번 게시물이 삭제되었습니다.".formatted(postId)
        );
    }

    @Operation(
            summary = "매칭 취소",
            description = "의뢰인은 본인의 매칭된 공고의 동행 매칭을 취소할 수 있으며, 관리자도 매칭을 취소할 수 있습니다."
    )
    @PatchMapping("/{postId}/matchedCancel")
    public RsData<PostDto> matchedCancel(
            @AuthenticationPrincipal SecurityUser actor,
            @PathVariable Long postId) {

        postService.matchedCancel(postId,actor.getId());

        return new RsData<>(
                "200-1",
                "%d번 게시물의 매칭이 취소되었습니다.".formatted(postId)
        );
    }

    @Operation(
            summary = "동행 완료 처리",
            description = "진행 중인 동행을 완료 처리하고 실제 동행 시간을 반영합니다."
    )
    @PatchMapping("/{postId}/escortComplete")
    public RsData<PostDto> escortComplete(
            @AuthenticationPrincipal SecurityUser actor,
            @PathVariable Long postId) {

        postService.escortComplete(postId,actor.getId());

        return new RsData<>(
                "200-1",
                "%d번 공고가 완료되었습니다. 실제 동행시간 및 지급액을 확인해주세요.".formatted(postId)
        );
    }
}
