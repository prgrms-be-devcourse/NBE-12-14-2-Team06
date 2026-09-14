package com.back.nbe12142team06.domain.post.service;

import com.back.nbe12142team06.domain.post.entity.Post;
import com.back.nbe12142team06.domain.post.repository.PostRepository;
import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Role;
import com.back.nbe12142team06.global.exception.InvalidException;
import com.back.nbe12142team06.global.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.back.nbe12142team06.domain.post.dto.PostWriteRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;

    public List<Post> findAll() {
        return postRepository.findAllWithClient();
    }

    public Post findById(Long id) {
        return postRepository.findByIdWithClient(id)
                .orElseThrow(() -> new NotFoundException("해당 공고가 없습니다."));
    }

    @Transactional
    public Post write(User user, PostWriteRequest reqBody) {

        if (user.getRole() != Role.CLIENT && user.getRole() != Role.ADMIN) {
            throw new InvalidException("공고 등록 권한이 없습니다. 의뢰인으로 로그인 해주세요.");
        }
        if (!reqBody.escortStartAt().isBefore(reqBody.escortEndAt())) {
            throw new InvalidException("동행 시작 시간은 종료 시간보다 빨라야 합니다.");
        }
        if (!reqBody.deadlineAt().isBefore(reqBody.escortStartAt())) {
            throw new InvalidException("모집 마감 시간은 동행 시작 시간보다 빨라야 합니다.");
        }

        Post post = Post.builder()
                .client(user)
                .title(reqBody.title())
                .content(reqBody.content())
                .region(reqBody.region())
                .hospitalName(reqBody.hospitalName())
                .hospitalAddress(reqBody.hospitalAddress())
                .hospitalLat(reqBody.hospitalLat())
                .hospitalLng(reqBody.hospitalLng())
                .pickupAddress(reqBody.pickupAddress())
                .pickupLat(reqBody.pickupLat())
                .pickupLng(reqBody.pickupLng())
                .hourlyPay(reqBody.hourlyPay())
                .escortStartAt(reqBody.escortStartAt())
                .escortEndAt(reqBody.escortEndAt())
                .deadlineAt(reqBody.deadlineAt())
                .patientNote(reqBody.patientNote())
                .reportRequired(reqBody.reportRequired())
                .build();

        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }


}