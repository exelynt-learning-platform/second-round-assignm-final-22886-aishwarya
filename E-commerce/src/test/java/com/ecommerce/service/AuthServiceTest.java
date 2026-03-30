package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private Authentication authentication;
    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");
        testUser = new User("John Doe", "john@example.com", "encodedPassword");
        testUser.setId(1L);
    }

    @Test
    void register_WithValidData_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        AuthResponse response = authService.register(registerRequest);
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("John Doe", response.getName());
    }

    @Test
    void register_WithExistingEmail_ShouldThrow() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        AuthResponse response = authService.login(loginRequest);
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrow() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid username or password"));
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
