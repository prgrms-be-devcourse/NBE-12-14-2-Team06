package com.back.nbe12142team06.domain.user.repository;


import com.back.nbe12142team06.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
