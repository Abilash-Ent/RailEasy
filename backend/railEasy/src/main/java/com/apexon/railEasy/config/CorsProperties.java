package com.apexon.railEasy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Externalized CORS settings, fully driven by {@code raileasy.cors} in
 * application.yml (single source of truth — no hard-coded values here).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "raileasy.cors")
public class CorsProperties {

    /** Allowed origin patterns (supports wildcards, e.g. http://localhost:*). */
    private List<String> allowedOrigins = List.of();

    /** HTTP methods permitted for cross-origin requests. */
    private List<String> allowedMethods = List.of();

    /** Request headers permitted for cross-origin requests. */
    private List<String> allowedHeaders = List.of();

    /** Response headers exposed to the browser (e.g. for reading a token header). */
    private List<String> exposedHeaders = List.of();

    /** Only enable when the frontend sends cookies; not needed for bearer tokens. */
    private boolean allowCredentials = false;

    /** Preflight cache duration in seconds. */
    private long maxAgeSeconds = 3600;
}



