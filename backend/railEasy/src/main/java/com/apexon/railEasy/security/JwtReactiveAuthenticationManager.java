package com.apexon.railEasy.security;

import com.apexon.railEasy.constants.AppConstants;
import com.apexon.railEasy.util.JwtService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Validates the JWT presented as the authentication credential and, if valid,
 * produces a fully authenticated token with the appropriate authorities.
 */
@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public JwtReactiveAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        return Mono.justOrEmpty(token)
                .filter(jwtService::isTokenValid)
                .map(validToken -> {
                    String username = jwtService.extractUsername(validToken);
                    String role = jwtService.extractRole(validToken);
                    var authorities = List.of(new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + role));
                    return (Authentication) new UsernamePasswordAuthenticationToken(username, validToken, authorities);
                });
    }
}

