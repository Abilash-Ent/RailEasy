package com.apexon.railEasy.dto.request;

import com.apexon.railEasy.constants.TravelClass;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload for booking specific seats on a schedule for a travel class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Please select a schedule")
    private Long scheduleId;

    @NotNull(message = "Travel class is required")
    private TravelClass travelClass;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(min = 1, max = 4, message = "Between 1 and 4 seats can be booked per booking")
    private List<String> seatNumbers;
}

