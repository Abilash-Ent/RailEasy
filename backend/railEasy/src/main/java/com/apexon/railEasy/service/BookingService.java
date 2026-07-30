package com.apexon.railEasy.service;

import com.apexon.railEasy.dto.request.BookingRequest;
import com.apexon.railEasy.dto.response.BookingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Ticket booking operations, always scoped to the authenticated user.
 */
public interface BookingService {

    Mono<BookingResponse> book(BookingRequest request, String username);

    Flux<BookingResponse> getMyBookings(String username);

    Mono<BookingResponse> getById(Long id, String username);

    /**
     * Generates a printable PDF e-ticket for one of the user's bookings.
     *
     * @param id       the booking id
     * @param username the authenticated user's email
     * @return the PDF document bytes
     */
    Mono<byte[]> downloadTicket(Long id, String username);

    Mono<BookingResponse> cancel(Long id, String username);
}

