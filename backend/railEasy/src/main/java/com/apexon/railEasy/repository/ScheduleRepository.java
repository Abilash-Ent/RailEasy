package com.apexon.railEasy.repository;

import com.apexon.railEasy.entity.Schedule;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Reactive repository for {@link Schedule} entities.
 */
@Repository
public interface ScheduleRepository extends ReactiveCrudRepository<Schedule, Long> {

    Flux<Schedule> findByTrainId(Long trainId);

    Mono<Boolean> existsByTrainId(Long trainId);

    /**
     * Returns {@code true} when the given train already has a schedule departing at the
     * same date and time. The {@code excludeId} lets updates ignore the row being edited
     * (pass a non-existent id such as {@code 0} for creates).
     */
    @Query("SELECT COUNT(*) > 0 FROM schedules " +
            "WHERE train_id = :trainId " +
            "AND journey_date = :journeyDate " +
            "AND departure_time = :departureTime " +
            "AND id <> :excludeId")
    Mono<Boolean> existsDuplicateSlot(Long trainId, LocalDate journeyDate,
                                      LocalTime departureTime, Long excludeId);

    @Query("SELECT * FROM schedules WHERE " +
            "LOWER(from_station) = LOWER(:from) " +
            "AND LOWER(to_station) = LOWER(:to) " +
            "AND journey_date = :date " +
            "ORDER BY departure_time")
    Flux<Schedule> search(String from, String to, LocalDate date);
}

