package com.back.nbe12142team06.domain.post.repository;

import com.back.nbe12142team06.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
}
