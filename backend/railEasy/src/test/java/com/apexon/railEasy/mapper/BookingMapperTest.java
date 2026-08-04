package com.apexon.railEasy.mapper;

import com.apexon.railEasy.constants.BookingStatus;
import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.response.BookingResponse;
import com.apexon.railEasy.entity.Booking;
import com.apexon.railEasy.entity.Schedule;
import com.apexon.railEasy.entity.Train;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    private static final List<String> SEATS = List.of("1A", "1B", "1C");

    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    private Booking booking() {
        return Booking.builder().id(10L).pnr("ABC12345").userId(5L).scheduleId(1L)
                .travelClass(TravelClass.AC_3)
                .totalFare(BigDecimal.valueOf(4200)).status(BookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.of(2026, 7, 28, 10, 30)).build();
    }

    @Test
    void toResponse_withScheduleAndTrain_mapsEverything() {
        Schedule schedule = Schedule.builder().id(1L).fromStation("Chennai").toStation("Mumbai")
                .journeyDate(LocalDate.of(2026, 8, 15)).build();
        Train train = Train.builder().id(2L).trainNumber("12621").trainName("Tamil Nadu Express").build();

        BookingResponse response = mapper.toResponse(booking(), schedule, train, SEATS);

        assertThat(response.getPnr()).isEqualTo("ABC12345");
        assertThat(response.getTrainNumber()).isEqualTo("12621");
        assertThat(response.getTrainName()).isEqualTo("Tamil Nadu Express");
        assertThat(response.getFromStation()).isEqualTo("Chennai");
        assertThat(response.getToStation()).isEqualTo("Mumbai");
        assertThat(response.getJourneyDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(response.getSeatNumbers()).containsExactly("1A", "1B", "1C");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void toResponse_withNullScheduleAndTrain_leavesEnrichedFieldsNull() {
        BookingResponse response = mapper.toResponse(booking(), null, null, SEATS);

        assertThat(response.getTrainNumber()).isNull();
        assertThat(response.getTrainName()).isNull();
        assertThat(response.getFromStation()).isNull();
        assertThat(response.getToStation()).isNull();
        assertThat(response.getJourneyDate()).isNull();
        assertThat(response.getSeatNumbers()).containsExactly("1A", "1B", "1C");
    }

    @Test
    void toResponse_withEmptySeats_mapsEmptyList() {
        BookingResponse response = mapper.toResponse(booking(), null, null, List.of());

        assertThat(response.getSeatNumbers()).isEmpty();
    }
}
