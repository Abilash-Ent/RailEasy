package com.apexon.railEasy.entity;

import com.apexon.railEasy.constants.BookingStatus;
import com.apexon.railEasy.constants.TravelClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A ticket booking for specific seats on a schedule/travel class.
 * Seat labels are stored as a comma-separated string (e.g. "1A,1B").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bookings")
public class Booking {

    @Id
    private Long id;

    @Column("pnr")
    private String pnr;

    @Column("user_id")
    private Long userId;

    @Column("schedule_id")
    private Long scheduleId;

    @Column("travel_class")
    private TravelClass travelClass;

    @Column("seat_numbers")
    private String seatNumbers;

    @Column("total_fare")
    private BigDecimal totalFare;

    @Column("status")
    private BookingStatus status;

    @Column("booked_at")
    private LocalDateTime bookedAt;
}

