package com.apexon.railEasy.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for creating or updating a train.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainRequest {

    @NotBlank(message = "Train number is required")
    private String trainNumber;

    @NotBlank(message = "Train name is required")
    private String trainName;

    @NotNull(message = "Total seats per class is required")
    @Min(value = 1, message = "Total seats per class must be at least 1")
    private Integer totalSeatsPerClass;
}

