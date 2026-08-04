package com.apexon.railEasy.mapper;

import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.request.ScheduleRequest;
import com.apexon.railEasy.dto.response.ScheduleResponse;
import com.apexon.railEasy.entity.Schedule;
import com.apexon.railEasy.entity.Train;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleMapperTest {

    private final ScheduleMapper mapper = Mappers.getMapper(ScheduleMapper.class);

    private ScheduleRequest request() {
        return ScheduleRequest.builder()
                .trainId(2L).fromStation("Chennai").toStation("Mumbai")
                .departureTime(LocalTime.of(22, 0)).arrivalTime(LocalTime.of(7, 15))
                .journeyDate(LocalDate.of(2026, 8, 15))
                .fareSleeper(BigDecimal.valueOf(800)).fareAc3(BigDecimal.valueOf(1400)).fareAc2(BigDecimal.valueOf(2000))
                .build();
    }

    private Schedule schedule() {
        return Schedule.builder().id(1L).trainId(2L)
                .fromStation("Chennai").toStation("Mumbai")
                .departureTime(LocalTime.of(22, 0)).arrivalTime(LocalTime.of(7, 15))
                .journeyDate(LocalDate.of(2026, 8, 15))
                .fareSleeper(BigDecimal.valueOf(800)).fareAc3(BigDecimal.valueOf(1400)).fareAc2(BigDecimal.valueOf(2000))
                .build();
    }

    @Test
    void toEntity_mapsAllFields() {
        Schedule entity = mapper.toEntity(request());

        assertThat(entity.getTrainId()).isEqualTo(2L);
        assertThat(entity.getFromStation()).isEqualTo("Chennai");
        assertThat(entity.getToStation()).isEqualTo("Mumbai");
        assertThat(entity.getDepartureTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(entity.getArrivalTime()).isEqualTo(LocalTime.of(7, 15));
        assertThat(entity.getJourneyDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(entity.getFareSleeper()).isEqualByComparingTo("800");
        assertThat(entity.getFareAc3()).isEqualByComparingTo("1400");
        assertThat(entity.getFareAc2()).isEqualByComparingTo("2000");
    }

    @Test
    void updateEntity_overwritesFields() {
        Schedule schedule = schedule();
        ScheduleRequest request = ScheduleRequest.builder()
                .trainId(3L).fromStation("Delhi").toStation("Pune")
                .departureTime(LocalTime.of(6, 0)).arrivalTime(LocalTime.of(20, 0))
                .journeyDate(LocalDate.of(2026, 9, 1))
                .fareSleeper(BigDecimal.valueOf(900)).fareAc3(BigDecimal.valueOf(1500)).fareAc2(BigDecimal.valueOf(2200))
                .build();

        mapper.updateEntity(schedule, request);

        assertThat(schedule.getId()).isEqualTo(1L);
        assertThat(schedule.getTrainId()).isEqualTo(3L);
        assertThat(schedule.getFromStation()).isEqualTo("Delhi");
        assertThat(schedule.getToStation()).isEqualTo("Pune");
        assertThat(schedule.getFareAc2()).isEqualByComparingTo("2200");
    }

    @Test
    void toResponse_withTrainAndAvailability_populatesFaresAndSeats() {
        Train train = Train.builder().id(2L).trainNumber("12621").trainName("Tamil Nadu Express").build();
        Map<TravelClass, Integer> availability = new EnumMap<>(TravelClass.class);
        availability.put(TravelClass.SLEEPER, 60);
        availability.put(TravelClass.AC_3, 62);
        availability.put(TravelClass.AC_2, 64);

        ScheduleResponse response = mapper.toResponse(schedule(), train, availability);

        assertThat(response.getTrainNumber()).isEqualTo("12621");
        assertThat(response.getTrainName()).isEqualTo("Tamil Nadu Express");
        assertThat(response.getFares()).containsEntry("SLEEPER", BigDecimal.valueOf(800))
                .containsEntry("AC_3", BigDecimal.valueOf(1400))
                .containsEntry("AC_2", BigDecimal.valueOf(2000));
        assertThat(response.getAvailableSeats()).containsEntry("SLEEPER", 60)
                .containsEntry("AC_3", 62)
                .containsEntry("AC_2", 64);
    }

    @Test
    void toResponse_withNullTrainAndNullAvailability_handlesGracefully() {
        ScheduleResponse response = mapper.toResponse(schedule(), null, null);

        assertThat(response.getTrainNumber()).isNull();
        assertThat(response.getTrainName()).isNull();
        assertThat(response.getFares()).hasSize(3);
        assertThat(response.getAvailableSeats()).isEmpty();
    }
}
