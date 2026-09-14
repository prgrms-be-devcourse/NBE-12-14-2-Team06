package com.back.nbe12142team06.domain.post.controller;

import com.back.nbe12142team06.domain.post.dto.PostDto;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import com.back.nbe12142team06.domain.post.dto.PostWriteResponse;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.repository.UserRepository;
import com.back.nbe12142team06.global.exception.NotFoundException;
import com.back.nbe12142team06.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    //공고목록조회
    @GetMapping
    public RsData<List<PostDto>> list() {
        List<PostDto> postDtoList = postService.findAll()
                .stream()
                .map(PostDto::new)
                .toList();

        return new RsData<>("200-1", "목록 조회 성공", postDtoList);
    }
    //공고상세조회
    @GetMapping("/{id}")
    public RsData<PostDto> detail(@PathVariable Long id) {
        PostDto postDto = new PostDto(postService.findById(id));

        return new RsData<>("200-1", "상세 조회 성공", postDto);
    }
    //공고등록
    @PostMapping
    @Transactional
    public RsData<PostWriteResponse> write(
            @RequestHeader("X-User-Id") Long userId, // TODO: 이후에 변경
            @RequestBody @Valid PostWriteRequest request) {

        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저가 없습니다."));
        Post post = postService.write(actor, request);
        return new RsData<>(
                "201-1",
                "%d번 글이 성공적으로 등록되었습니다".formatted(post.getId()),
                new PostWriteResponse(post)
        );
    }
    //공고수정
   /* @PatchMapping
    @Transactional
    public RsData<PostDto> modify(@PathVariable Long id) {

        User actor = rq.getActor();
        Post post = postService.findById(id).get();
        post.checkUserModify(actor);

        postService.modify(post, reqBody.title, reqBody.content);

        return new RsData<>(
                "200-1",
                "%d번 게시물이 수정되었습니다.".formatted(id)
        );
    }*/
    //공고삭제
    /*@DeleteMapping("/{id}")
    @Transactional
    public RsData<PostDto> delete(@PathVariable Long id) {
        User actor = rq.getActor();
        Post post = postService.findById(id).get();
        post.checkUserDelete(actor);

        postService.delete(id);

        return new RsData<>(
                "200-1",
                "%d번 게시물이 삭제되었습니다.".formatted(id)
        );*/
}
