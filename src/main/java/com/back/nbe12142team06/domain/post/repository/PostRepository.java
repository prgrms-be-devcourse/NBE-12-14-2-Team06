package com.back.nbe12142team06.domain.post.repository;

import com.back.nbe12142team06.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p JOIN FETCH p.client ORDER BY p.id DESC")
    List<Post> findAllWithClient();

    @Query("SELECT p FROM Post p JOIN FETCH p.client WHERE p.id = :id")
    Optional<Post> findByIdWithClient(@Param("id") Long id);
}
