package com.apexon.railEasy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Train master data. Stations, timings and fares belong to {@link Schedule}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("trains")
public class Train {

    @Id
    private Long id;

    @Column("train_number")
    private String trainNumber;

    @Column("train_name")
    private String trainName;

    @Column("total_seats_per_class")
    private Integer totalSeatsPerClass;

    @Column("active")
    private Boolean active;
}

