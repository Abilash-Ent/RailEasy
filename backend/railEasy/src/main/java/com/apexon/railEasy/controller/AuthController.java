package com.apexon.railEasy.controller;

import com.apexon.railEasy.constants.AppConstants;
import com.apexon.railEasy.dto.request.LoginRequest;
import com.apexon.railEasy.dto.request.RegisterRequest;
import com.apexon.railEasy.dto.response.AuthResponse;
import com.apexon.railEasy.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Authentication endpoints: registration and login.
 */
@Tag(name = "Authentication", description = "User registration and login")
@RestController
@RequestMapping(AppConstants.AUTH_BASE)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user and receive a JWT")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Authenticate and receive a JWT")
    @PostMapping("/login")
    public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Logout (stateless JWT — client discards the token)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public Mono<Void> logout() {
        // Stateless JWT: there is no server-side session to invalidate. The client
        // simply discards the token. A token blacklist could be added if required.
        return Mono.empty();
    }
}

