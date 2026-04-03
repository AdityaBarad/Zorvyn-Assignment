package com.example.zorvyn.controller;

import com.example.zorvyn.dto.request.CreateUserRequest;
import com.example.zorvyn.dto.request.UpdateUserRequest;
import com.example.zorvyn.dto.response.ApiResponse;
import com.example.zorvyn.dto.response.UserResponse;
import com.example.zorvyn.model.enums.RoleName;
import com.example.zorvyn.service.interfaces.UserService;
import com.example.zorvyn.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User Management", description = "CRUD for users — Admin only")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        log.info("REQUEST: POST /api/v1/users by: {}", SecurityUtils.getCurrentUserEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (paginated)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REQUEST: GET /api/v1/users by: {}", SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(
                ApiResponse.success(userService.getAllUsers(pageable), "Users retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Get user by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getUserById(id), "User retrieved"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("REQUEST: PUT /api/v1/users/{} by: {}", id, SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(
                ApiResponse.success(userService.updateUser(id, request), "User updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user (soft delete)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deactivated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        log.info("REQUEST: DELETE /api/v1/users/{} by: {}", id, SecurityUtils.getCurrentUserEmail());
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated"));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign role to user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role assigned")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable Long id,
            @RequestParam RoleName roleName) {
        userService.assignRole(id, roleName);
        return ResponseEntity.ok(ApiResponse.success(null, "Role assigned successfully"));
    }
}
