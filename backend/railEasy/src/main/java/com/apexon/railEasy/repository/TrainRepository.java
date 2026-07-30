package com.apexon.railEasy.repository;

import com.apexon.railEasy.entity.Train;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for {@link Train} entities.
 */
@Repository
public interface TrainRepository extends ReactiveCrudRepository<Train, Long> {

    Mono<Train> findByTrainNumber(String trainNumber);

    Mono<Boolean> existsByTrainNumber(String trainNumber);
}

