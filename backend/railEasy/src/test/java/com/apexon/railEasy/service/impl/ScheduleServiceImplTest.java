package com.apexon.railEasy.service.impl;

import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.request.ScheduleRequest;
import com.apexon.railEasy.dto.response.ScheduleResponse;
import com.apexon.railEasy.entity.Schedule;
import com.apexon.railEasy.entity.Train;
import com.apexon.railEasy.exception.BusinessValidationException;
import com.apexon.railEasy.exception.ResourceNotFoundException;
import com.apexon.railEasy.mapper.ScheduleMapper;
import com.apexon.railEasy.cache.TrainCache;
import com.apexon.railEasy.repository.BookingRepository;
import com.apexon.railEasy.repository.BookingSeatRepository;
import com.apexon.railEasy.repository.ScheduleRepository;
import com.apexon.railEasy.repository.TrainRepository;
import com.apexon.railEasy.service.SeatAvailabilityHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock private ScheduleRepository scheduleRepository;
    @Mock private TrainRepository trainRepository;
    @Mock private TrainCache trainCache;
    @Mock private BookingRepository bookingRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;
    @Mock private ScheduleMapper scheduleMapper;
    @Mock private SeatAvailabilityHelper seatAvailabilityHelper;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private Schedule schedule() {
        return Schedule.builder().id(1L).trainId(2L)
                .fromStation("Chennai").toStation("Mumbai")
                .departureTime(LocalTime.of(22, 0)).arrivalTime(LocalTime.of(7, 15))
                .journeyDate(LocalDate.now().plusDays(10))
                .fareSleeper(BigDecimal.valueOf(800)).fareAc3(BigDecimal.valueOf(1400)).fareAc2(BigDecimal.valueOf(2000))
                .build();
    }

    @Test
    void getSeatAvailability_returnsBookedAndAvailableSeats() {
        when(scheduleRepository.findById(1L)).thenReturn(Mono.just(schedule()));
        when(seatAvailabilityHelper.bookedSeats(1L, TravelClass.AC_3)).thenReturn(Mono.just(Set.of("1A", "1B")));

        StepVerifier.create(scheduleService.getSeatAvailability(1L, TravelClass.AC_3))
                .assertNext(seatMap -> {
                    org.assertj.core.api.Assertions.assertThat(seatMap.getTotalSeats()).isEqualTo(64);
                    org.assertj.core.api.Assertions.assertThat(seatMap.getBookedSeats()).containsExactlyInAnyOrder("1A", "1B");
                    org.assertj.core.api.Assertions.assertThat(seatMap.getAvailableSeats()).hasSize(62);
                    org.assertj.core.api.Assertions.assertThat(seatMap.getAvailableSeats()).doesNotContain("1A", "1B");
                })
                .verifyComplete();
    }

    @Test
    void getSeatAvailability_failsWhenScheduleMissing() {
        when(scheduleRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(scheduleService.getSeatAvailability(99L, TravelClass.SLEEPER))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void create_failsWhenTrainMissing() {
        ScheduleRequest request = ScheduleRequest.builder()
                .trainId(2L).fromStation("Chennai").toStation("Mumbai")
                .departureTime(LocalTime.of(22, 0)).arrivalTime(LocalTime.of(7, 15))
                .journeyDate(LocalDate.now().plusDays(10))
                .fareSleeper(BigDecimal.valueOf(800)).fareAc3(BigDecimal.valueOf(1400)).fareAc2(BigDecimal.valueOf(2000))
                .build();
        when(trainRepository.findById(2L)).thenReturn(Mono.empty());

        StepVerifier.create(scheduleService.create(request))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void create_rejectsSameFromAndToStation() {
        ScheduleRequest request = ScheduleRequest.builder()
                .trainId(2L).fromStation("Chennai").toStation("chennai")
                .departureTime(LocalTime.of(22, 0)).arrivalTime(LocalTime.of(7, 15))
                .journeyDate(LocalDate.now().plusDays(10))
                .fareSleeper(BigDecimal.valueOf(800)).fareAc3(BigDecimal.valueOf(1400)).fareAc2(BigDecimal.valueOf(2000))
                .build();

        StepVerifier.create(scheduleService.create(request))
                .expectError(BusinessValidationException.class)
                .verify();
    }

    @Test
    void search_returnsEnrichedSchedules() {
        ScheduleResponse response = ScheduleResponse.builder().id(1L).trainNumber("12621").build();
        when(scheduleRepository.search("Chennai", "Mumbai", LocalDate.of(2026, 8, 15)))
                .thenReturn(Flux.just(schedule()));
        when(trainCache.findAllById(anyCollection())).thenReturn(Flux.just(
                Train.builder().id(2L).trainNumber("12621").totalSeatsPerClass(64).build()));
        when(bookingSeatRepository.findByScheduleIdIn(anyCollection())).thenReturn(Flux.empty());
        when(scheduleMapper.toResponse(any(Schedule.class), any(Train.class), any())).thenReturn(response);

        StepVerifier.create(scheduleService.search("Chennai", "Mumbai", LocalDate.of(2026, 8, 15)))
                .expectNext(response)
                .verifyComplete();
    }
}
