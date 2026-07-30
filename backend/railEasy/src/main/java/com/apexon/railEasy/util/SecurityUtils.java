package com.apexon.railEasy.util;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

/**
 * Helper methods for accessing the currently authenticated principal reactively.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * @return the username (email) of the currently authenticated user.
     */
    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName());
    }
}

