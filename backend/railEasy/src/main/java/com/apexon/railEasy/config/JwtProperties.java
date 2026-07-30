package com.apexon.railEasy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized JWT configuration (bound from application.yml under {@code raileasy.jwt}).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "raileasy.jwt")
public class JwtProperties {

    /** Base64/plain secret used to sign tokens (must be >= 32 chars for HS256). */
    private String secret;

    /** Token validity in milliseconds. */
    private long expirationMs;

    /** Token issuer name. */
    private String issuer = "railEasy";
}

