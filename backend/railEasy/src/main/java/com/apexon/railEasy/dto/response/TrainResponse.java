package com.apexon.railEasy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public representation of a train.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainResponse {
    private Long id;
    private String trainNumber;
    private String trainName;
    private Integer totalSeatsPerClass;
    private Boolean active;
}

