package com.apexon.railEasy.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Returns a JSON 401 response when an unauthenticated request hits a secured endpoint.
 */
@Component
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ReactiveJsonErrorWriter errorWriter;

    public JwtAuthenticationEntryPoint(ReactiveJsonErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return errorWriter.write(exchange, HttpStatus.UNAUTHORIZED,
                "Authentication is required to access this resource",
                "Provide a valid JWT in the 'Authorization: Bearer <token>' header. "
                        + "Obtain one via POST /api/v1/auth/login.");
    }
}

