package com.example.zorvyn.controller;

import com.example.zorvyn.dto.request.LoginRequest;
import com.example.zorvyn.dto.response.ApiResponse;
import com.example.zorvyn.dto.response.AuthResponse;
import com.example.zorvyn.service.interfaces.AuthService;
import com.example.zorvyn.util.AppConstants;
import com.example.zorvyn.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Authentication", description = "Login, token refresh, and logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and receive JWT tokens")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("REQUEST: POST /api/v1/auth/login for: {}", request.getEmail());
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Invalid refresh token", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader(AppConstants.REFRESH_TOKEN_HEADER) String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out successfully")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(AppConstants.HEADER_STRING) String token) {
        String email = SecurityUtils.getCurrentUserEmail();
        log.info("REQUEST: POST /api/v1/auth/logout by: {}", email);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
}
