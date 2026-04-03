package com.example.zorvyn.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String status,
        List<String> roles,
        LocalDateTime createdAt
) {
}

