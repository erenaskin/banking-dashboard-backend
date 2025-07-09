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
import com.erenaskin.banking_dashboard.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token);
    }
}
