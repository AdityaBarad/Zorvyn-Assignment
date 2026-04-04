package com.example.zorvyn.service;

import com.example.zorvyn.dto.request.LoginRequest;
import com.example.zorvyn.dto.response.AuthResponse;
import com.example.zorvyn.exception.InvalidOperationException;
import com.example.zorvyn.model.entity.User;
import com.example.zorvyn.repository.UserRepository;
import com.example.zorvyn.security.CustomUserDetailsService;
import com.example.zorvyn.security.JwtTokenProvider;
import com.example.zorvyn.service.impl.AuthServiceImpl;
import com.example.zorvyn.service.interfaces.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_withValidCredentials_returnsAuthResponseWithTokens() {
        // given
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        HttpServletRequest httpRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);
        User user = User.builder().id(77L).email("user@example.com").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class));
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiryMs()).thenReturn(3600L);
        when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com"))
                .thenReturn(Optional.of(user));
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        // when
        AuthResponse response = authService.login(request, httpRequest);

        // then
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        verify(auditService).log(eq("LOGIN"), eq("USER"), eq(77L), any());
    }

    @Test
    void login_withBadCredentials_propagatesException() {
        // given
        LoginRequest request = new LoginRequest("user@example.com", "badpass");
        HttpServletRequest httpRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when
        // then
        assertThrows(BadCredentialsException.class, () -> authService.login(request, httpRequest));
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void logout_withToken_removesRefreshToken() {
        // given
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        HttpServletRequest httpRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);
        User user = User.builder().id(44L).email("user@example.com").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class));
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiryMs()).thenReturn(3600L);
        when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com"))
                .thenReturn(Optional.of(user));
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        authService.login(request, httpRequest);

        when(jwtTokenProvider.extractEmail("access-token")).thenReturn("user@example.com");
        when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com"))
                .thenReturn(Optional.of(user));

        // when
        authService.logout("Bearer access-token");

        // then
        assertThrows(InvalidOperationException.class,
                () -> authService.refreshToken("refresh-token"));
        verify(auditService).log(eq("LOGOUT"), eq("USER"), eq(44L), any());
    }

    @Test
    void refreshToken_withInvalidToken_throwsException() {
        // given
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        // when
        // then
        assertThrows(InvalidOperationException.class,
                () -> authService.refreshToken("bad-token"));
    }
}

