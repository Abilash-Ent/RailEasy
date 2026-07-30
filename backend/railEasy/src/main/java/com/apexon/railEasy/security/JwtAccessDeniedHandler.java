package com.apexon.railEasy.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Returns a JSON 403 response when an authenticated user lacks the required authority.
 */
@Component
public class JwtAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ReactiveJsonErrorWriter errorWriter;

    public JwtAccessDeniedHandler(ReactiveJsonErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return errorWriter.write(exchange, HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource",
                "Your account role is not allowed to perform this operation "
                        + "(admin-only endpoints require the ADMIN role).");
    }
}

