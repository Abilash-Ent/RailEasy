package com.apexon.railEasy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/**
 * Public representation of a schedule, enriched with train info and per-class
 * availability/fare for the search results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long id;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String fromStation;
    private String toStation;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private LocalDate journeyDate;

    /** Fare per travel class (SLEEPER/AC_3/AC_2). */
    private Map<String, BigDecimal> fares;

    /** Available seat count per travel class. */
    private Map<String, Integer> availableSeats;
}

