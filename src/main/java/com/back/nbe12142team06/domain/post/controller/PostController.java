package com.back.nbe12142team06.domain.post.controller;


import com.back.nbe12142team06.domain.post.dto.PostDto;
import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.service.PostService;
import com.back.nbe12142team06.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    /* 공고목록조회 */
    @GetMapping
    public RsData<List<PostDto>> list() {
        List<PostDto> postDtoList = postService.findAll()
                .stream()
                .map(PostDto::new)
                .toList();

        return new RsData<>("200-1", "목록 조회 성공", postDtoList);
    }

    /* 공고 상세 조회 */
    @GetMapping("/{id}")
    public RsData<PostDto> detail(@PathVariable Long id) {
        PostDto postDto = new PostDto(postService.findById(id));

        return new RsData<>("200-1", "상세 조회 성공", postDto);
    }

}
