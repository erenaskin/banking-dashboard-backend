package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.AuthResponse;
import com.erenaskin.banking_dashboard.dto.LoginRequest;
import com.erenaskin.banking_dashboard.dto.RegisterRequest;
import com.erenaskin.banking_dashboard.entity.Role;
import com.erenaskin.banking_dashboard.entity.User;
import com.erenaskin.banking_dashboard.mapper.UserMapper;
import com.erenaskin.banking_dashboard.repository.UserRepository;
import com.erenaskin.banking_dashboard.security.JwtUtil;
import com.erenaskin.banking_dashboard.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        LoginRequest request = new LoginRequest("user@example.com", "correctpass");
        User user = new User();
        user.setPassword("encodedPass");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void register_ShouldSaveUserAndReturnToken_WhenEmailNotExists() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        User userEntity = new User();
        userEntity.setEmail(request.getEmail());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateToken(userEntity)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(Role.USER, userEntity.getRole());
        assertEquals("encodedPassword", userEntity.getPassword());

        verify(userRepository).save(userEntity);
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequest request = new LoginRequest("invalid@example.com", "pass");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Email already in use", ex.getMessage());
    }
}
