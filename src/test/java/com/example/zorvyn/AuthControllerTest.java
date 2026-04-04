package com.example.zorvyn;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.zorvyn.config.SecurityConfig;
import com.example.zorvyn.controller.AuthController;
import com.example.zorvyn.dto.request.LoginRequest;
import com.example.zorvyn.dto.response.AuthResponse;
import com.example.zorvyn.exception.GlobalExceptionHandler;
import com.example.zorvyn.security.CustomUserDetailsService;
import com.example.zorvyn.security.JwtAuthenticationFilter;
import com.example.zorvyn.security.JwtTokenProvider;
import com.example.zorvyn.service.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_withValidCredentials_returns200() throws Exception {
        LoginRequest req = new LoginRequest("admin@finance.com", "Admin@1234");
        AuthResponse mockResponse = new AuthResponse("access.token.here",
                "refresh.token.here", "Bearer", 900000L);
        given(authService.login(any(), any(HttpServletRequest.class))).willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access.token.here"));
    }

    @Test
    void login_withBlankEmail_returns400() throws Exception {
        LoginRequest req = new LoginRequest("", "Admin@1234");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_withBadCredentials_returns401() throws Exception {
        LoginRequest req = new LoginRequest("admin@finance.com", "WrongPass1!");
        given(authService.login(any(), any(HttpServletRequest.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
