package com.apexon.railEasy.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Payload for creating or updating a schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @NotNull(message = "Please select a train")
    private Long trainId;

    @NotBlank(message = "From station is required")
    private String fromStation;

    @NotBlank(message = "To station is required")
    private String toStation;

    @NotNull(message = "Departure time is required")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalTime arrivalTime;

    @NotNull(message = "Journey date is required")
    @Future(message = "Journey date must be in the future")
    private LocalDate journeyDate;

    @NotNull(message = "Sleeper fare is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Sleeper fare must be greater than 0")
    private BigDecimal fareSleeper;

    @NotNull(message = "AC 3-tier fare is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "AC 3-tier fare must be greater than 0")
    private BigDecimal fareAc3;

    @NotNull(message = "AC 2-tier fare is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "AC 2-tier fare must be greater than 0")
    private BigDecimal fareAc2;
}

