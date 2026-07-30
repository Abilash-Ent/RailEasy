package com.apexon.railEasy.service;

import com.apexon.railEasy.dto.request.LoginRequest;
import com.apexon.railEasy.dto.request.RegisterRequest;
import com.apexon.railEasy.dto.response.AuthResponse;
import reactor.core.publisher.Mono;

/**
 * Authentication and registration operations.
 */
public interface AuthService {

    Mono<AuthResponse> register(RegisterRequest request);

    Mono<AuthResponse> login(LoginRequest request);
}

