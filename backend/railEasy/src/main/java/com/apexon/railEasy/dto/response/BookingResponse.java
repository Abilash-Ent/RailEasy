package com.apexon.railEasy.dto.response;

import com.apexon.railEasy.constants.BookingStatus;
import com.apexon.railEasy.constants.TravelClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Public representation of a booking (a ticket).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String pnr;
    private Long userId;
    private Long scheduleId;
    private String trainNumber;
    private String trainName;
    private String fromStation;
    private String toStation;
    private LocalDate journeyDate;
    private TravelClass travelClass;
    private List<String> seatNumbers;
    private BigDecimal totalFare;
    private BookingStatus status;
    private LocalDateTime bookedAt;
}

