package com.apexon.railEasy.cache;

import com.apexon.railEasy.config.TrainCacheProperties;
import com.apexon.railEasy.entity.Train;
import com.apexon.railEasy.repository.TrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainCacheTest {

    @Mock private TrainRepository trainRepository;

    private TrainCache trainCache;

    private static Train train(long id, String number) {
        return Train.builder().id(id).trainNumber(number).trainName("Express")
                .totalSeatsPerClass(64).active(true).build();
    }

    @BeforeEach
    void setUp() {
        trainCache = new TrainCache(trainRepository, new TrainCacheProperties());
    }

    @Test
    void findById_fetchesFromRepositoryOnMissThenServesFromCache() {
        when(trainRepository.findById(2L)).thenReturn(Mono.just(train(2L, "12621")));

        // First call: cache miss -> repository is hit.
        StepVerifier.create(trainCache.findById(2L))
                .expectNextMatches(t -> t.getId() == 2L)
                .verifyComplete();

        // Second call: served from cache, repository not touched again.
        StepVerifier.create(trainCache.findById(2L))
                .expectNextMatches(t -> t.getId() == 2L)
                .verifyComplete();

        verify(trainRepository, times(1)).findById(2L);
    }

    @Test
    void findById_returnsEmptyWhenTrainAbsent() {
        when(trainRepository.findById(9L)).thenReturn(Mono.empty());

        StepVerifier.create(trainCache.findById(9L))
                .verifyComplete();
    }

    @Test
    void findAllById_servesHitsFromCacheAndFetchesOnlyMisses() {
        // Warm the cache with train 2.
        when(trainRepository.findById(2L)).thenReturn(Mono.just(train(2L, "12621")));
        StepVerifier.create(trainCache.findById(2L)).expectNextCount(1).verifyComplete();

        // Now request 2 (hit) and 3 (miss); only 3 should be fetched.
        when(trainRepository.findAllById(anyCollection())).thenReturn(Flux.just(train(3L, "12622")));

        StepVerifier.create(trainCache.findAllById(List.of(2L, 3L)))
                .expectNextCount(2)
                .verifyComplete();

        verify(trainRepository, times(1)).findAllById(List.of(3L));
    }

    @Test
    void findAllById_skipsRepositoryWhenAllCached() {
        when(trainRepository.findById(2L)).thenReturn(Mono.just(train(2L, "12621")));
        StepVerifier.create(trainCache.findById(2L)).expectNextCount(1).verifyComplete();

        StepVerifier.create(trainCache.findAllById(List.of(2L)))
                .expectNextCount(1)
                .verifyComplete();

        verify(trainRepository, never()).findAllById(anyCollection());
    }

    @Test
    void evict_forcesRefetchOnNextLookup() {
        when(trainRepository.findById(2L)).thenReturn(Mono.just(train(2L, "12621")));
        StepVerifier.create(trainCache.findById(2L)).expectNextCount(1).verifyComplete();

        trainCache.evict(2L);

        StepVerifier.create(trainCache.findById(2L)).expectNextCount(1).verifyComplete();

        // Once before eviction, once after -> two repository hits.
        verify(trainRepository, times(2)).findById(2L);
    }

    @Test
    void evict_ignoresNullId() {
        trainCache.evict(null);
        verifyNoInteractions(trainRepository);
    }

    @Test
    void findAll_loadsOnceThenServesFromSnapshotAndWarmsByIdCache() {
        when(trainRepository.findAll()).thenReturn(Flux.just(train(2L, "12621"), train(3L, "12622")));

        // First call loads via the supplier.
        StepVerifier.create(trainCache.findAll(trainRepository::findAll))
                .expectNextCount(2)
                .verifyComplete();

        // Second call is served from the cached snapshot (loader not invoked again).
        StepVerifier.create(trainCache.findAll(trainRepository::findAll))
                .expectNextCount(2)
                .verifyComplete();

        verify(trainRepository, times(1)).findAll();

        // The by-id cache was warmed, so findById(2) needs no repository call.
        StepVerifier.create(trainCache.findById(2L)).expectNextCount(1).verifyComplete();
        verify(trainRepository, never()).findById(2L);
    }

    @Test
    void evictList_forcesSnapshotReload() {
        when(trainRepository.findAll()).thenReturn(Flux.just(train(2L, "12621")));
        StepVerifier.create(trainCache.findAll(trainRepository::findAll)).expectNextCount(1).verifyComplete();

        trainCache.evictList();

        StepVerifier.create(trainCache.findAll(trainRepository::findAll)).expectNextCount(1).verifyComplete();

        verify(trainRepository, times(2)).findAll();
    }

    @Test
    void evict_alsoClearsListSnapshot() {
        when(trainRepository.findAll()).thenReturn(Flux.just(train(2L, "12621")));
        StepVerifier.create(trainCache.findAll(trainRepository::findAll)).expectNextCount(1).verifyComplete();

        trainCache.evict(2L);

        StepVerifier.create(trainCache.findAll(trainRepository::findAll)).expectNextCount(1).verifyComplete();

        verify(trainRepository, times(2)).findAll();
    }
}
