package com.apexon.railEasy.repository;

import com.apexon.railEasy.entity.Booking;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for {@link Booking} entities.
 */
@Repository
public interface BookingRepository extends ReactiveCrudRepository<Booking, Long> {

    Flux<Booking> findByUserIdOrderByBookedAtDesc(Long userId);

    Mono<Boolean> existsByScheduleId(Long scheduleId);
}
