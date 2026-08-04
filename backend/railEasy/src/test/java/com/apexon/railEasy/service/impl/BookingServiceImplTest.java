package com.apexon.railEasy.service.impl;

import com.apexon.railEasy.cache.TrainCache;
import com.apexon.railEasy.constants.BookingStatus;
import com.apexon.railEasy.constants.Role;
import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.request.BookingRequest;
import com.apexon.railEasy.dto.response.BookingResponse;
import com.apexon.railEasy.entity.Booking;
import com.apexon.railEasy.entity.Schedule;
import com.apexon.railEasy.entity.Train;
import com.apexon.railEasy.entity.User;
import com.apexon.railEasy.exception.BusinessValidationException;
import com.apexon.railEasy.exception.ResourceNotFoundException;
import com.apexon.railEasy.mapper.BookingMapper;
import com.apexon.railEasy.repository.BookingRepository;
import com.apexon.railEasy.repository.BookingSeatRepository;
import com.apexon.railEasy.repository.ScheduleRepository;
import com.apexon.railEasy.repository.UserRepository;
import com.apexon.railEasy.service.SeatAvailabilityHelper;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private TrainCache trainCache;
    @Mock private UserRepository userRepository;
    @Mock private SeatAvailabilityHelper seatAvailabilityHelper;
    @Mock private BookingMapper bookingMapper;
    @Mock private com.apexon.railEasy.util.TicketPdfGenerator ticketPdfGenerator;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private static final String USERNAME = "user@raileasy.com";
    private User user;
    private Schedule schedule;
    private Train train;
    private BookingResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder().id(5L).email(USERNAME).role(Role.USER).build();
        schedule = Schedule.builder().id(1L).trainId(2L)
                .fromStation("Chennai").toStation("Mumbai").journeyDate(LocalDate.now().plusDays(10))
                .fareSleeper(BigDecimal.valueOf(800)).fareAc3(BigDecimal.valueOf(100)).fareAc2(BigDecimal.valueOf(2000))
                .build();
        train = Train.builder().id(2L).trainNumber("12621").trainName("Tamil Nadu Express")
                .totalSeatsPerClass(64).active(true).build();
        response = BookingResponse.builder().id(10L).pnr("ABC12345").build();
    }

    private BookingRequest request(List<String> seats) {
        return BookingRequest.builder()
                .scheduleId(1L).travelClass(TravelClass.AC_3).seatNumbers(seats).build();
    }

    @Test
    void book_confirmsWhenSeatsAvailable() {
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(scheduleRepository.findById(1L)).thenReturn(Mono.just(schedule));
        when(seatAvailabilityHelper.bookedSeats(anyLong(), any(TravelClass.class))).thenReturn(Mono.just(Set.of()));
        Booking saved = Booking.builder().id(10L).pnr("ABC12345").userId(5L).scheduleId(1L)
                .travelClass(TravelClass.AC_3).totalFare(BigDecimal.valueOf(200))
                .status(BookingStatus.CONFIRMED).bookedAt(LocalDateTime.now()).build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(saved));
        when(bookingSeatRepository.saveAll(anyIterable())).thenReturn(Flux.empty());
        when(trainCache.findById(2L)).thenReturn(Mono.just(train));
        when(bookingMapper.toResponse(any(Booking.class), any(Schedule.class), any(Train.class), any()))
                .thenReturn(response);

        StepVerifier.create(bookingService.book(request(List.of("1A", "1B")), USERNAME))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void book_rejectsAlreadyBookedSeats() {
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(scheduleRepository.findById(1L)).thenReturn(Mono.just(schedule));
        when(seatAvailabilityHelper.bookedSeats(anyLong(), any(TravelClass.class)))
                .thenReturn(Mono.just(Set.of("1A")));

        StepVerifier.create(bookingService.book(request(List.of("1A", "1B")), USERNAME))
                .expectError(BusinessValidationException.class)
                .verify();

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void book_rejectsInvalidSeatLabel() {
        StepVerifier.create(bookingService.book(request(List.of("9Z")), USERNAME))
                .expectError(BusinessValidationException.class)
                .verify();
    }

    @Test
    void book_rejectsMoreThanFourSeats() {
        StepVerifier.create(bookingService.book(request(List.of("1A", "1B", "1C", "1D", "1E")), USERNAME))
                .expectError(BusinessValidationException.class)
                .verify();
    }

    @Test
    void book_failsWhenScheduleMissing() {
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(scheduleRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.book(request(List.of("1A")), USERNAME))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void cancel_setsStatusCancelledForOwner() {
        Booking booking = Booking.builder().id(10L).pnr("ABC12345").userId(5L).scheduleId(1L)
                .travelClass(TravelClass.AC_3).status(BookingStatus.CONFIRMED).build();
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(bookingRepository.findById(10L)).thenReturn(Mono.just(booking));
        when(bookingSeatRepository.findByBookingId(10L)).thenReturn(Flux.empty());
        when(bookingSeatRepository.deleteByBookingId(10L)).thenReturn(Mono.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(booking));
        when(scheduleRepository.findById(1L)).thenReturn(Mono.just(schedule));
        when(trainCache.findById(2L)).thenReturn(Mono.just(train));
        when(bookingMapper.toResponse(any(Booking.class), any(Schedule.class), any(Train.class), any()))
                .thenReturn(response);

        StepVerifier.create(bookingService.cancel(10L, USERNAME))
                .expectNext(response)
                .verifyComplete();

        org.assertj.core.api.Assertions.assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancel_failsWhenBookingBelongsToAnotherUser() {
        Booking booking = Booking.builder().id(10L).userId(999L).scheduleId(1L)
                .status(BookingStatus.CONFIRMED).build();
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(bookingRepository.findById(10L)).thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.cancel(10L, USERNAME))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancel_failsWhenAlreadyCancelled() {
        Booking booking = Booking.builder().id(10L).userId(5L).scheduleId(1L)
                .status(BookingStatus.CANCELLED).build();
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(bookingRepository.findById(10L)).thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.cancel(10L, USERNAME))
                .expectError(BusinessValidationException.class)
                .verify();

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getMyBookings_returnsOnlyCurrentUsersBookings() {
        Booking booking = Booking.builder().id(10L).userId(5L).scheduleId(1L)
                .travelClass(TravelClass.AC_3).status(BookingStatus.CONFIRMED).build();
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(bookingRepository.findByUserIdOrderByBookedAtDesc(5L)).thenReturn(Flux.just(booking));
        when(bookingSeatRepository.findByBookingIdIn(anyCollection())).thenReturn(Flux.empty());
        when(scheduleRepository.findAllById(anyIterable())).thenReturn(Flux.just(schedule));
        when(trainCache.findAllById(anyCollection())).thenReturn(Flux.just(train));
        when(bookingMapper.toResponse(any(Booking.class), any(Schedule.class), any(Train.class), any()))
                .thenReturn(response);

        StepVerifier.create(bookingService.getMyBookings(USERNAME))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void downloadTicket_returnsPdfBytesForOwner() {
        Booking booking = Booking.builder().id(10L).pnr("ABC12345").userId(5L).scheduleId(1L)
                .travelClass(TravelClass.AC_3).status(BookingStatus.CONFIRMED).build();
        byte[] pdf = {1, 2, 3};
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(bookingRepository.findById(10L)).thenReturn(Mono.just(booking));
        when(bookingSeatRepository.findByBookingId(10L)).thenReturn(Flux.empty());
        when(scheduleRepository.findById(1L)).thenReturn(Mono.just(schedule));
        when(trainCache.findById(2L)).thenReturn(Mono.just(train));
        when(bookingMapper.toResponse(any(Booking.class), any(Schedule.class), any(Train.class), any()))
                .thenReturn(response);
        when(ticketPdfGenerator.generate(response)).thenReturn(pdf);

        StepVerifier.create(bookingService.downloadTicket(10L, USERNAME))
                .expectNext(pdf)
                .verifyComplete();
    }

    @Test
    void downloadTicket_failsWhenBookingBelongsToAnotherUser() {
        Booking booking = Booking.builder().id(10L).userId(999L).scheduleId(1L)
                .status(BookingStatus.CONFIRMED).build();
        when(userRepository.findByEmail(USERNAME)).thenReturn(Mono.just(user));
        when(bookingRepository.findById(10L)).thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.downloadTicket(10L, USERNAME))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}


