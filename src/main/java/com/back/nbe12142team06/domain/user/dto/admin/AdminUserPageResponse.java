package com.back.nbe12142team06.domain.user.dto.admin;

import com.back.nbe12142team06.domain.user.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public record AdminUserPageResponse(
        List<AdminUserResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
){
    public AdminUserPageResponse(Page<User> page){
        this(
                page.getContent().stream()
                        .map(AdminUserResponse::new)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
