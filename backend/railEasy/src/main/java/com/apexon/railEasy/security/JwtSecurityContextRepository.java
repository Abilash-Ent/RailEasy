package com.apexon.railEasy.security;

import com.apexon.railEasy.constants.AppConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Stateless security context repository that extracts the bearer token from the
 * {@code Authorization} header and delegates validation to the authentication manager.
 */
@Component
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtReactiveAuthenticationManager authenticationManager;

    public JwtSecurityContextRepository(JwtReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // Stateless: nothing to persist.
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(AppConstants.TOKEN_PREFIX)) {
            return Mono.empty();
        }
        String token = header.substring(AppConstants.TOKEN_PREFIX.length());
        Authentication auth = new UsernamePasswordAuthenticationToken(token, token);
        return authenticationManager.authenticate(auth)
                .map(SecurityContextImpl::new);
    }
}

