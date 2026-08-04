package com.apexon.railEasy.entity;

import com.apexon.railEasy.constants.TravelClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A single booked seat. Normalized out of {@link Booking} so the database can
 * enforce no-double-booking (unique per schedule + class + seat) and compute
 * availability with COUNT/GROUP BY. Rows exist only for active bookings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("booking_seats")
public class BookingSeat {

    @Id
    private Long id;

    @Column("booking_id")
    private Long bookingId;

    @Column("schedule_id")
    private Long scheduleId;

    @Column("travel_class")
    private TravelClass travelClass;

    @Column("seat_no")
    private String seatNo;
}

