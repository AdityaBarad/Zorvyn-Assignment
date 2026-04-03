package com.example.zorvyn.service.impl;

import com.example.zorvyn.dto.request.LoginRequest;
import com.example.zorvyn.dto.response.AuthResponse;
import com.example.zorvyn.exception.InvalidOperationException;
import com.example.zorvyn.model.entity.User;
import com.example.zorvyn.repository.UserRepository;
import com.example.zorvyn.security.CustomUserDetailsService;
import com.example.zorvyn.security.JwtTokenProvider;
import com.example.zorvyn.service.interfaces.AuditService;
import com.example.zorvyn.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for: {}", request.getEmail());
            throw e;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        refreshTokenStore.put(refreshToken, request.getEmail());

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow();
        String ip = httpRequest.getRemoteAddr();
        auditService.log("LOGIN", "USER", user.getId(), "Login from IP: " + ip);

        log.info("Successful login for: {}", request.getEmail());
        return new AuthResponse(accessToken, refreshToken, "Bearer",
                jwtTokenProvider.getAccessTokenExpiryMs());
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidOperationException("Invalid or expired refresh token");
        }

        String email = refreshTokenStore.get(refreshToken);
        if (email == null) {
            throw new InvalidOperationException("Refresh token not recognized — please login again");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        return new AuthResponse(newAccessToken, refreshToken, "Bearer",
                jwtTokenProvider.getAccessTokenExpiryMs());
    }

    @Override
    public void logout(String token) {
        String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtTokenProvider.extractEmail(bearerToken);
        refreshTokenStore.values().removeIf(v -> v.equals(email));
        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
        if (user != null) {
            auditService.log("LOGOUT", "USER", user.getId(), "User logged out");
        }
        log.info("User logged out: {}", email);
    }
}

