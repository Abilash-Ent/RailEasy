package com.apexon.railEasy.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.test.StepVerifier;

class SecurityUtilsTest {

    @Test
    void getCurrentUsername_returnsAuthenticatedPrincipalName() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user@raileasy.com", "n/a");

        StepVerifier.create(SecurityUtils.getCurrentUsername()
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .expectNext("user@raileasy.com")
                .verifyComplete();
    }

    @Test
    void getCurrentUsername_emitsEmptyWhenNoSecurityContext() {
        StepVerifier.create(SecurityUtils.getCurrentUsername())
                .verifyComplete();
    }
}

