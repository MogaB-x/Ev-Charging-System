package com.chargeflow.auth.service;

import com.chargeflow.auth.dto.AuthResponse;
import com.chargeflow.auth.dto.LoginRequest;
import com.chargeflow.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
