package com.apexon.railEasy.dto.response;

import com.apexon.railEasy.constants.TravelClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Seat map for a schedule + travel class: the full layout plus which seats are
 * already booked, so the UI can render the 8x8 grid.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityResponse {
    private Long scheduleId;
    private TravelClass travelClass;
    private int rows;
    private int columns;
    private int totalSeats;
    private List<String> allSeats;
    private List<String> bookedSeats;
    private List<String> availableSeats;
}

