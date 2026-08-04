package com.apexon.railEasy.service;

import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.entity.BookingSeat;
import com.apexon.railEasy.repository.BookingSeatRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes which seats are currently occupied for a schedule + travel class,
 * derived from normalized {@link BookingSeat} rows (which exist only for active
 * bookings; cancelling a booking releases its seats).
 */
@Component
public class SeatAvailabilityHelper {

    private final BookingSeatRepository bookingSeatRepository;

    public SeatAvailabilityHelper(BookingSeatRepository bookingSeatRepository) {
        this.bookingSeatRepository = bookingSeatRepository;
    }

    public Mono<Set<String>> bookedSeats(Long scheduleId, TravelClass travelClass) {
        return bookingSeatRepository.findByScheduleIdAndTravelClass(scheduleId, travelClass)
                .map(BookingSeat::getSeatNo)
                .collect(Collectors.toSet());
    }
}
