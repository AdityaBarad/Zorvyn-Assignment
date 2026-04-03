package com.example.zorvyn.service.impl;

import com.example.zorvyn.dto.request.CreateUserRequest;
import com.example.zorvyn.dto.request.UpdateUserRequest;
import com.example.zorvyn.dto.response.UserResponse;
import com.example.zorvyn.exception.DuplicateResourceException;
import com.example.zorvyn.exception.InvalidOperationException;
import com.example.zorvyn.exception.ResourceNotFoundException;
import com.example.zorvyn.model.entity.Role;
import com.example.zorvyn.model.entity.User;
import com.example.zorvyn.model.enums.RoleName;
import com.example.zorvyn.model.enums.UserStatus;
import com.example.zorvyn.repository.RoleRepository;
import com.example.zorvyn.repository.UserRepository;
import com.example.zorvyn.service.interfaces.AuditService;
import com.example.zorvyn.service.interfaces.UserService;
import com.example.zorvyn.util.AppConstants;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "User already exists with email: " + request.getEmail());
        }
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + request.getRoleName()));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(role)))
                .build();

        User saved = userRepository.save(user);
        auditService.log("USER_CREATED", "USER", saved.getId(),
                "User created: " + saved.getEmail());
        log.info("Created user: {}", saved.getEmail());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        validatePageSize(pageable.getPageSize());
        return userRepository.findAllByDeletedAtIsNull(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRoleName() != null) {
            Role role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Role not found: " + request.getRoleName()));
            user.setRoles(new HashSet<>(Set.of(role)));
        }

        User saved = userRepository.save(user);
        auditService.log("USER_UPDATED", "USER", saved.getId(), "User updated");
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setStatus(UserStatus.INACTIVE);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        auditService.log("USER_DEACTIVATED", "USER", id, "User deactivated");
        log.info("Deactivated user id: {}", id);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, RoleName roleName) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + roleName));
        user.getRoles().add(role);
        userRepository.save(user);
        auditService.log("ROLE_ASSIGNED", "USER", userId,
                "Role assigned: " + roleName.name());
    }

    private void validatePageSize(int size) {
        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new InvalidOperationException(
                    "Page size must not exceed " + AppConstants.MAX_PAGE_SIZE);
        }
    }
}

