package com.example.zorvyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void login_withValidCredentials_returnsAuthResponse() {
        LoginRequest req = new LoginRequest("admin@finance.com", "Admin@1234");
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
        when(authenticationManager.authenticate(any()))
                .thenReturn(mock(Authentication.class));

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("admin@finance.com");
        when(userDetails.getAuthorities()).thenReturn(List.of());

        User mockUser = User.builder().id(1L).email("admin@finance.com").build();
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access.token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh.token");
        when(jwtTokenProvider.getAccessTokenExpiryMs()).thenReturn(900000L);
        when(userRepository.findByEmailAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(mockUser));

        AuthResponse result = authService.login(req, httpReq);

        assertNotNull(result);
        assertEquals("access.token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
    }

    @Test
    void login_withBadCredentials_throwsException() {
        LoginRequest req = new LoginRequest("admin@finance.com", "wrong");
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(req, httpReq));
    }

    @Test
    void refreshToken_withValidToken_returnsNewAccessToken() {
        String refreshToken = "valid.refresh.token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("new.access.token");
        when(jwtTokenProvider.getAccessTokenExpiryMs()).thenReturn(900000L);

        assertThrows(InvalidOperationException.class,
                () -> authService.refreshToken(refreshToken));
    }
}

