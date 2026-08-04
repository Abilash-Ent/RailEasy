package com.apexon.railEasy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Externalized settings for the in-memory train reference-data cache,
 * driven by {@code raileasy.cache.train} in application.yml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "raileasy.cache.train")
public class TrainCacheProperties {

    /** Maximum number of trains to hold in the cache. */
    private long maximumSize = 500;

    /** Time-to-live after a cache entry is written. */
    private Duration expireAfterWrite = Duration.ofMinutes(10);
}

