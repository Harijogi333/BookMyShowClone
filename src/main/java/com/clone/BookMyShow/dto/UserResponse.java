package com.clone.BookMyShow.dto;

import com.clone.BookMyShow.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;
}
