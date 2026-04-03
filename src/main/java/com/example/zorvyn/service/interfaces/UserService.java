package com.example.zorvyn.service.interfaces;

import com.example.zorvyn.dto.request.CreateUserRequest;
import com.example.zorvyn.dto.request.UpdateUserRequest;
import com.example.zorvyn.dto.response.UserResponse;
import com.example.zorvyn.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deactivateUser(Long id);

    void assignRole(Long userId, RoleName roleName);
}

