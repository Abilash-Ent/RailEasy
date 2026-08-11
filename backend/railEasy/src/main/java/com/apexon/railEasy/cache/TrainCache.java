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
import java.util.function.Supplier;

/**
 * Small read-through cache for {@link Train} reference data, which changes rarely
 * but is read on every schedule/booking enrichment and on the train list/detail
 * endpoints. Backed by Caffeine (bounded size + TTL) and kept reactive-friendly:
 * values (not publishers) are cached, and cache misses fall through to the
 * repository.
 *
 * <p>Two views are cached: a by-id map ({@link #findById}/{@link #findAllById}) and
 * the full-list snapshot ({@link #findAll}). Because any single-train change also
 * changes the list, {@link #evict(Long)} clears both views; {@link #evictList()}
 * clears just the list (used after inserting a brand-new train).
 */
@Component
public class TrainCache {

    /** Single-entry key for the cached "all trains" snapshot. */
    private static final Boolean ALL_KEY = Boolean.TRUE;

    private final TrainRepository trainRepository;
    private final Cache<Long, Train> cache;
    private final Cache<Boolean, List<Train>> listCache;

    public TrainCache(TrainRepository trainRepository, TrainCacheProperties properties) {
        this.trainRepository = trainRepository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.getMaximumSize())
                .expireAfterWrite(properties.getExpireAfterWrite())
                .build();
        this.listCache = Caffeine.newBuilder()
                .maximumSize(1)
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

    /**
     * Returns all trains, serving the cached snapshot when present. On a miss the
     * {@code loader} is used, the snapshot is cached and the by-id cache is warmed
     * so subsequent {@link #findById} calls hit as well.
     */
    public Flux<Train> findAll(Supplier<Flux<Train>> loader) {
        List<Train> cached = listCache.getIfPresent(ALL_KEY);
        if (cached != null) {
            return Flux.fromIterable(cached);
        }
        return loader.get().collectList()
                .doOnNext(list -> {
                    listCache.put(ALL_KEY, list);
                    list.forEach(train -> cache.put(train.getId(), train));
                })
                .flatMapMany(Flux::fromIterable);
    }

    /** Invalidate a single train and the list snapshot (call after update/delete). */
    public void evict(Long id) {
        if (id != null) {
            cache.invalidate(id);
            listCache.invalidateAll();
        }
    }

    /** Invalidate only the cached "all trains" snapshot (call after insert). */
    public void evictList() {
        listCache.invalidateAll();
    }
}
