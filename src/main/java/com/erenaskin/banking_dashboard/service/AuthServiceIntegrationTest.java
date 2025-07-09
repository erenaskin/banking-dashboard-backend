package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.AuthResponse;
import com.erenaskin.banking_dashboard.dto.LoginRequest;
import com.erenaskin.banking_dashboard.dto.RegisterRequest;
import com.erenaskin.banking_dashboard.entity.User;
import com.erenaskin.banking_dashboard.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    void register_ShouldReturnToken() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "12345678");
        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
    }

    @Test
    @Order(2)
    void login_ShouldReturnToken() {
        // Kayıt
        User user = new User();
        user.setFullName("Test");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        userRepository.save(user);

        // Giriş
        LoginRequest login = new LoginRequest("test@example.com", "12345678");
        AuthResponse response = authService.login(login);

        assertNotNull(response);
        assertNotNull(response.getToken());
    }
}
