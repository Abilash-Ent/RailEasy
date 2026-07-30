package com.apexon.railEasy.service;

import com.apexon.railEasy.constants.BookingStatus;
import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.mapper.BookingMapper;
import com.apexon.railEasy.repository.BookingRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes which seats are currently occupied for a schedule + travel class,
 * derived from CONFIRMED bookings (cancelled bookings free their seats).
 */
@Component
public class SeatAvailabilityHelper {

    private final BookingRepository bookingRepository;

    public SeatAvailabilityHelper(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Mono<Set<String>> bookedSeats(Long scheduleId, TravelClass travelClass) {
        return bookingRepository
                .findByScheduleIdAndTravelClassAndStatus(scheduleId, travelClass, BookingStatus.CONFIRMED)
                .flatMapIterable(b -> BookingMapper.parseSeats(b.getSeatNumbers()))
                .collect(Collectors.toSet());
    }
}

