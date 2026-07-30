package com.apexon.railEasy.util;

import com.apexon.railEasy.constants.BookingStatus;
import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.response.BookingResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TicketPdfGeneratorTest {

    private final TicketPdfGenerator generator = new TicketPdfGenerator();

    private BookingResponse confirmedBooking() {
        return BookingResponse.builder()
                .id(10L)
                .pnr("ABC12345")
                .userId(5L)
                .scheduleId(1L)
                .trainNumber("12621")
                .trainName("Tamil Nadu Express")
                .fromStation("Chennai")
                .toStation("Mumbai")
                .journeyDate(LocalDate.of(2026, 8, 15))
                .travelClass(TravelClass.AC_3)
                .seatNumbers(List.of("1A", "1B"))
                .totalFare(BigDecimal.valueOf(2800))
                .status(BookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.of(2026, 7, 28, 10, 30))
                .build();
    }

    @Test
    void generate_producesNonEmptyPdfDocument() {
        byte[] pdf = generator.generate(confirmedBooking());

        assertThat(pdf).isNotNull().isNotEmpty();
        // Every valid PDF begins with the "%PDF-" magic header.
        String header = new String(pdf, 0, 5, StandardCharsets.US_ASCII);
        assertThat(header).isEqualTo("%PDF-");
    }

    @Test
    void generate_producesPdfForCancelledBooking() {
        BookingResponse cancelled = confirmedBooking();
        cancelled.setStatus(BookingStatus.CANCELLED);

        byte[] pdf = generator.generate(cancelled);

        assertThat(pdf).isNotNull().isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generate_handlesMissingOptionalFieldsGracefully() {
        BookingResponse sparse = BookingResponse.builder()
                .id(11L)
                .pnr("XYZ99999")
                .status(BookingStatus.CONFIRMED)
                .build();

        byte[] pdf = generator.generate(sparse);

        assertThat(pdf).isNotNull().isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generate_rejectsNullBooking() {
        assertThatNullPointerException().isThrownBy(() -> generator.generate(null));
    }
}

