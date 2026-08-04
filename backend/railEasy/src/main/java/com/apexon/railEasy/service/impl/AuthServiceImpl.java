package com.apexon.railEasy.service.impl;

import com.apexon.railEasy.constants.Role;
import com.apexon.railEasy.dto.request.LoginRequest;
import com.apexon.railEasy.dto.request.RegisterRequest;
import com.apexon.railEasy.dto.response.AuthResponse;
import com.apexon.railEasy.entity.User;
import com.apexon.railEasy.exception.DuplicateResourceException;
import com.apexon.railEasy.exception.InvalidCredentialsException;
import com.apexon.railEasy.repository.UserRepository;
import com.apexon.railEasy.service.AuthService;
import com.apexon.railEasy.util.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Default {@link AuthService} implementation.
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<AuthResponse> register(RegisterRequest request) {
        return userRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new DuplicateResourceException(
                                "An account with this email already exists. Please log in instead."));
                    }
                    User user = User.builder()
                            .fullName(request.getFullName())
                            .email(request.getEmail())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .role(Role.USER)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(user);
                })
                .map(this::buildAuthResponse)
                .doOnSuccess(r -> log.info("Registered new user: {}", request.getEmail()));
    }

    @Override
    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(this::buildAuthResponse)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid email or password")));
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getExpirationSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
