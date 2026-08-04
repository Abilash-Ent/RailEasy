package com.apexon.railEasy.repository;

import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.entity.BookingSeat;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Reactive repository for normalized {@link BookingSeat} rows.
 */
@Repository
public interface BookingSeatRepository extends ReactiveCrudRepository<BookingSeat, Long> {

    /** Seats occupied on a schedule for a travel class (availability + clash checks). */
    Flux<BookingSeat> findByScheduleIdAndTravelClass(Long scheduleId, TravelClass travelClass);

    /** Occupancy across many schedules at once (batch enrichment). */
    Flux<BookingSeat> findByScheduleIdIn(Collection<Long> scheduleIds);

    /** Seats belonging to a single booking. */
    Flux<BookingSeat> findByBookingId(Long bookingId);

    /** Seats belonging to many bookings at once (batch enrichment). */
    Flux<BookingSeat> findByBookingIdIn(Collection<Long> bookingIds);

    /** Releases a booking's seats (called on cancellation). */
    Mono<Void> deleteByBookingId(Long bookingId);
}

