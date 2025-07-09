package com.erenaskin.banking_dashboard.service;

import com.erenaskin.banking_dashboard.dto.AuthResponse;
import com.erenaskin.banking_dashboard.dto.LoginRequest;
import com.erenaskin.banking_dashboard.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
