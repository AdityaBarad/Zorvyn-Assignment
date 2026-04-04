package com.example.zorvyn.controller;

import com.example.zorvyn.dto.request.LoginRequest;
import com.example.zorvyn.dto.response.AuthResponse;
import com.example.zorvyn.exception.GlobalExceptionHandler;
import com.example.zorvyn.service.interfaces.AuthService;
import com.example.zorvyn.security.CustomUserDetailsService;
import com.example.zorvyn.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void login_withValidCredentials_returnsOkWithToken() throws Exception {
        // given
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        AuthResponse response = new AuthResponse("access-token", "refresh-token", "Bearer", 3600L);
        when(authService.login(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenReturn(response);

        // when
        // then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(authService).login(any(LoginRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void login_withBlankEmail_returnsBadRequest() throws Exception {
        // given
        LoginRequest request = new LoginRequest("", "password123");

        // when
        // then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(authService);
    }

    @Test
    void login_withWrongCredentials_returnsUnauthorized() throws Exception {
        // given
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        when(authService.login(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when
        // then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    void login_withMissingBody_returnsBadRequest() throws Exception {
        // given

        // when
        // then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Invalid request body or unrecognized enum value"));
    }
}
