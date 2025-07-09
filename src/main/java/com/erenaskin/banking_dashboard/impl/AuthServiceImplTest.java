package com.erenaskin.banking_dashboard.impl;

import com.erenaskin.banking_dashboard.dto.AuthResponse;
import com.erenaskin.banking_dashboard.dto.LoginRequest;
import com.erenaskin.banking_dashboard.dto.RegisterRequest;
import com.erenaskin.banking_dashboard.entity.Role;
import com.erenaskin.banking_dashboard.entity.User;
import com.erenaskin.banking_dashboard.exception.AuthenticationFailedException;
import com.erenaskin.banking_dashboard.exception.UserAlreadyExistsException;
import com.erenaskin.banking_dashboard.mapper.UserMapper;
import com.erenaskin.banking_dashboard.repository.UserRepository;
import com.erenaskin.banking_dashboard.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserMapper userMapper;

    @Mock
    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_ShouldThrow_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest("test","test@example.com", "password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void register_ShouldSaveUserAndReturnToken_WhenEmailNotExists() {
        RegisterRequest request = new RegisterRequest("new user","newuser@example.com", "pass123");
        User userEntity = new User();
        userEntity.setEmail(request.getEmail());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");
        when(jwtUtil.generateToken(userEntity)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        assertEquals(Role.USER, userEntity.getRole());
        assertEquals("encodedPass", userEntity.getPassword());

        verify(userRepository).save(userEntity);
    }

    @Test
    void login_ShouldThrow_WhenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown@example.com", "pass");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class, () -> authService.login(request));
    }

    @Test
    void login_ShouldThrow_WhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("user@example.com", "wrongpass");
        User user = new User();
        user.setPassword("encodedPass");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(AuthenticationFailedException.class, () -> authService.login(request));
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
}
