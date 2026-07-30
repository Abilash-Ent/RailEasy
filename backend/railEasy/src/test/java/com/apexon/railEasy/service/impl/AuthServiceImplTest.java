package com.apexon.railEasy.service.impl;

import com.apexon.railEasy.constants.Role;
import com.apexon.railEasy.dto.request.LoginRequest;
import com.apexon.railEasy.dto.request.RegisterRequest;
import com.apexon.railEasy.entity.User;
import com.apexon.railEasy.exception.DuplicateResourceException;
import com.apexon.railEasy.exception.InvalidCredentialsException;
import com.apexon.railEasy.repository.UserRepository;
import com.apexon.railEasy.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L).fullName("Rail User").email("user@raileasy.com")
                .password("hashed").role(Role.USER).createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_createsUser_andReturnsToken() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Rail User").email("user@raileasy.com").password("secret123").build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(existingUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        StepVerifier.create(authService.register(request))
                .assertNext(response -> {
                    org.assertj.core.api.Assertions.assertThat(response.getToken()).isEqualTo("jwt-token");
                    org.assertj.core.api.Assertions.assertThat(response.getRole()).isEqualTo(Role.USER);
                    org.assertj.core.api.Assertions.assertThat(response.getEmail()).isEqualTo("user@raileasy.com");
                })
                .verifyComplete();
    }

    @Test
    void register_rejectsDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Rail User").email("user@raileasy.com").password("secret123").build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(Mono.just(true));

        StepVerifier.create(authService.register(request))
                .expectError(DuplicateResourceException.class)
                .verify();
    }

    @Test
    void login_succeedsWithValidCredentials() {
        LoginRequest request = LoginRequest.builder().email("user@raileasy.com").password("secret123").build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Mono.just(existingUser));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(existingUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        StepVerifier.create(authService.login(request))
                .assertNext(response ->
                        org.assertj.core.api.Assertions.assertThat(response.getToken()).isEqualTo("jwt-token"))
                .verifyComplete();
    }

    @Test
    void login_failsWithWrongPassword() {
        LoginRequest request = LoginRequest.builder().email("user@raileasy.com").password("wrong").build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Mono.just(existingUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        StepVerifier.create(authService.login(request))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    void login_failsWhenUserNotFound() {
        LoginRequest request = LoginRequest.builder().email("missing@raileasy.com").password("secret123").build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Mono.empty());

        StepVerifier.create(authService.login(request))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }
}

