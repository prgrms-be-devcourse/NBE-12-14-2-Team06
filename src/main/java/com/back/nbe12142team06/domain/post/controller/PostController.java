package com.back.nbe12142team06.domain.post.controller;

import com.back.nbe12142team06.domain.post.dto.PostDto;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.dto.PostWriteResponse;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.response.RsData;
import com.back.nbe12142team06.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
//TODO:공고등록OPEN>매칭MATCHED>동행시작IN_PROGRESS>종료COMPLETED
//TODO:공고등록>취소CANCELED 1.의뢰인취소 2.관리자취소 -> 한 플로우
//TODO:공고등록>의뢰인이결제완료>매칭MATCHED(지원자 한명선택)>지원상태(지원승인)>취소(모집중 상태에서만 취소 가능 : 신청 동행인 에게 알림 서비스)
//TODO:공고등록> 모집마감시간까지 매칭안됨 >. 만료(오늘날짜확인해서 배치or스케쥴러)
//TODO:공고목록>결제취소된애들은 안보이게
    //결제 취소시 기존행 취소일자 update 되고 새행이 새로 생기므로 즉 결제 행 2개 생성 됨
    //공고목록조회
    @GetMapping
    public RsData<Page<PostDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostDto> postDtoPage = postService.findAll(PageRequest.of(page, size))
                .map(PostDto::new);

        return new RsData<>("200-1", "목록 조회 성공", postDtoPage);
    }

    //공고상세조회
    @GetMapping("/{postId}")
    public RsData<PostDto> detail(@PathVariable Long postId) {
        PostDto postDto = new PostDto(postService.findById(postId));

        return new RsData<>("200-1", "상세 조회 성공", postDto);
    }

    //공고등록
    @PostMapping
    public RsData<PostWriteResponse> write(
            @AuthenticationPrincipal SecurityUser actor,
            @RequestBody @Valid PostWriteRequest request) {

        Post post = postService.write(actor.getId(), request);
        return new RsData<>(
                "201-1",
                "%d번 글이 성공적으로 등록되었습니다".formatted(post.getId()),
                new PostWriteResponse(post)
        );
    }

    //공고수정 : 모집시작전까지 수정가능
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
    //공고삭제
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
}
