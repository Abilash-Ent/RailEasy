package com.apexon.railEasy.controller;

import com.apexon.railEasy.constants.AppConstants;
import com.apexon.railEasy.dto.request.BookingRequest;
import com.apexon.railEasy.dto.response.BookingResponse;
import com.apexon.railEasy.service.BookingService;
import com.apexon.railEasy.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Booking endpoints for any authenticated user (USER or ADMIN). Each operation is
 * scoped to the caller's own bookings.
 */
@Tag(name = "Bookings", description = "Ticket booking, viewing and cancellation")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(AppConstants.BOOKING_BASE)
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Book seats on a schedule for a travel class")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<BookingResponse> book(@Valid @RequestBody BookingRequest request) {
        return SecurityUtils.getCurrentUsername()
                .flatMap(username -> bookingService.book(request, username));
    }

    @Operation(summary = "List the current user's bookings (My Tickets)")
    @GetMapping("/mine")
    public Flux<BookingResponse> myBookings() {
        return SecurityUtils.getCurrentUsername()
                .flatMapMany(bookingService::getMyBookings);
    }

    @Operation(summary = "Get one of the current user's bookings by id")
    @GetMapping("/{id}")
    public Mono<BookingResponse> getById(@PathVariable Long id) {
        return SecurityUtils.getCurrentUsername()
                .flatMap(username -> bookingService.getById(id, username));
    }

    @Operation(summary = "Cancel a booking by id")
    @PutMapping("/{id}/cancel")
    public Mono<BookingResponse> cancel(@PathVariable Long id) {
        return SecurityUtils.getCurrentUsername()
                .flatMap(username -> bookingService.cancel(id, username));
    }

    @Operation(summary = "Download a booking as a PDF e-ticket")
    @GetMapping(value = "/{id}/ticket", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadTicket(@PathVariable Long id) {
        return SecurityUtils.getCurrentUsername()
                .flatMap(username -> bookingService.downloadTicket(id, username))
                .map(pdf -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                                .filename("raileasy-ticket-" + id + ".pdf").build().toString())
                        .body(pdf));
    }
}

