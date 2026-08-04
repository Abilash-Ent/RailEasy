package com.apexon.railEasy.service;

import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.entity.BookingSeat;
import com.apexon.railEasy.repository.BookingSeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatAvailabilityHelperTest {

    @Mock private BookingSeatRepository bookingSeatRepository;

    @InjectMocks
    private SeatAvailabilityHelper seatAvailabilityHelper;

    private static BookingSeat seat(String seatNo) {
        return BookingSeat.builder()
                .scheduleId(1L).travelClass(TravelClass.AC_3).seatNo(seatNo).build();
    }

    @Test
    void bookedSeats_collectsSeatNumbersForScheduleAndClass() {
        when(bookingSeatRepository.findByScheduleIdAndTravelClass(1L, TravelClass.AC_3))
                .thenReturn(Flux.just(seat("1A"), seat("1B")));

        StepVerifier.create(seatAvailabilityHelper.bookedSeats(1L, TravelClass.AC_3))
                .assertNext(seats ->
                        org.assertj.core.api.Assertions.assertThat(seats)
                                .containsExactlyInAnyOrder("1A", "1B"))
                .verifyComplete();
    }

    @Test
    void bookedSeats_returnsEmptySetWhenNothingBooked() {
        when(bookingSeatRepository.findByScheduleIdAndTravelClass(1L, TravelClass.SLEEPER))
                .thenReturn(Flux.empty());

        StepVerifier.create(seatAvailabilityHelper.bookedSeats(1L, TravelClass.SLEEPER))
                .assertNext(seats -> org.assertj.core.api.Assertions.assertThat(seats).isEmpty())
                .verifyComplete();
    }
}

