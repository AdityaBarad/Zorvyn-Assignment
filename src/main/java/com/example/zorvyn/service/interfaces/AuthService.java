package com.example.zorvyn.service.interfaces;

import com.example.zorvyn.dto.request.LoginRequest;
import com.example.zorvyn.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refreshToken(String refreshToken);

    void logout(String token);
}
