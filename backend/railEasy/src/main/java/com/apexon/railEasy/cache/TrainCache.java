package com.apexon.railEasy.cache;

import com.apexon.railEasy.config.TrainCacheProperties;
import com.apexon.railEasy.entity.Train;
import com.apexon.railEasy.repository.TrainRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Small read-through cache for {@link Train} reference data, which changes rarely
 * but is read on every schedule/booking enrichment. Backed by Caffeine (bounded
 * size + TTL) and kept reactive-friendly: values (not publishers) are cached, and
 * cache misses fall through to the repository.
 */
@Component
public class TrainCache {

    private final TrainRepository trainRepository;
    private final Cache<Long, Train> cache;

    public TrainCache(TrainRepository trainRepository, TrainCacheProperties properties) {
        this.trainRepository = trainRepository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.getMaximumSize())
                .expireAfterWrite(properties.getExpireAfterWrite())
                .build();
    }

    /** Returns a single train, serving from cache when present. */
    public Mono<Train> findById(Long id) {
        Train cached = cache.getIfPresent(id);
        if (cached != null) {
            return Mono.just(cached);
        }
        return trainRepository.findById(id).doOnNext(train -> cache.put(id, train));
    }

    /** Returns many trains, serving cache hits directly and fetching only the misses. */
    public Flux<Train> findAllById(Collection<Long> ids) {
        List<Train> hits = new ArrayList<>();
        List<Long> misses = new ArrayList<>();
        for (Long id : ids) {
            Train cached = cache.getIfPresent(id);
            if (cached != null) {
                hits.add(cached);
            } else {
                misses.add(id);
            }
        }
        Flux<Train> missFlux = misses.isEmpty()
                ? Flux.empty()
                : trainRepository.findAllById(misses).doOnNext(train -> cache.put(train.getId(), train));
        return Flux.fromIterable(hits).concatWith(missFlux);
    }

    /** Invalidate a single train (call after update/delete). */
    public void evict(Long id) {
        if (id != null) {
            cache.invalidate(id);
        }
    }
}

