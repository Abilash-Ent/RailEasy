package com.apexon.railEasy.entity;

import com.apexon.railEasy.constants.TravelClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A dated run of a {@link Train} between two stations, with per-class fares.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("schedules")
public class Schedule {

    @Id
    private Long id;

    @Column("train_id")
    private Long trainId;

    @Column("from_station")
    private String fromStation;

    @Column("to_station")
    private String toStation;

    @Column("departure_time")
    private LocalTime departureTime;

    @Column("arrival_time")
    private LocalTime arrivalTime;

    @Column("journey_date")
    private LocalDate journeyDate;

    @Column("fare_sleeper")
    private BigDecimal fareSleeper;

    @Column("fare_ac3")
    private BigDecimal fareAc3;

    @Column("fare_ac2")
    private BigDecimal fareAc2;

    /** Convenience: resolve the fare for a given travel class. */
    public BigDecimal fareFor(TravelClass travelClass) {
        return switch (travelClass) {
            case SLEEPER -> fareSleeper;
            case AC_3 -> fareAc3;
            case AC_2 -> fareAc2;
        };
    }
}

