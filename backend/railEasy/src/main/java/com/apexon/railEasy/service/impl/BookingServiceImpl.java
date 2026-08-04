package com.apexon.railEasy.service.impl;

import com.apexon.railEasy.cache.TrainCache;
import com.apexon.railEasy.constants.AppConstants;
import com.apexon.railEasy.constants.BookingStatus;
import com.apexon.railEasy.dto.request.BookingRequest;
import com.apexon.railEasy.dto.response.BookingResponse;
import com.apexon.railEasy.entity.Booking;
import com.apexon.railEasy.entity.BookingSeat;
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
import com.apexon.railEasy.service.BookingService;
import com.apexon.railEasy.service.SeatAvailabilityHelper;
import com.apexon.railEasy.util.PnrGenerator;
import com.apexon.railEasy.util.SeatUtils;
import com.apexon.railEasy.util.TicketPdfGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default {@link BookingService} implementation. Booking validates seat
 * selection against the fixed 8x8 layout and current occupancy, and persists
 * seats as normalized {@link BookingSeat} rows (the unique constraint guarantees
 * no double-booking even under concurrency).
 */
@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private static final String BOOKING_NOT_FOUND =
            "We couldn't find the requested booking. Please check and try again.";
    private static final String SEATS_JUST_TAKEN =
            "One or more of the selected seats were just booked by someone else. "
                    + "Please pick different seats and try again.";

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ScheduleRepository scheduleRepository;
    private final TrainCache trainCache;
    private final UserRepository userRepository;
    private final SeatAvailabilityHelper seatAvailabilityHelper;
    private final BookingMapper bookingMapper;
    private final TicketPdfGenerator ticketPdfGenerator;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              BookingSeatRepository bookingSeatRepository,
                              ScheduleRepository scheduleRepository,
                              TrainCache trainCache,
                              UserRepository userRepository,
                              SeatAvailabilityHelper seatAvailabilityHelper,
                              BookingMapper bookingMapper,
                              TicketPdfGenerator ticketPdfGenerator) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.scheduleRepository = scheduleRepository;
        this.trainCache = trainCache;
        this.userRepository = userRepository;
        this.seatAvailabilityHelper = seatAvailabilityHelper;
        this.bookingMapper = bookingMapper;
        this.ticketPdfGenerator = ticketPdfGenerator;
    }

    @Override
    @Transactional
    public Mono<BookingResponse> book(BookingRequest request, String username) {
        return Mono.fromCallable(() -> normalizeAndValidateSeats(request))
                .flatMap(requestedSeats -> currentUser(username)
                        .flatMap(user -> scheduleRepository.findById(request.getScheduleId())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                        "The selected schedule is no longer available. "
                                                + "Please choose another train or schedule.")))
                                .flatMap(schedule -> reserve(schedule, request, requestedSeats, user))));
    }

    private Mono<BookingResponse> reserve(Schedule schedule, BookingRequest request,
                                          List<String> requestedSeats, User user) {
        return seatAvailabilityHelper.bookedSeats(schedule.getId(), request.getTravelClass())
                .flatMap(booked -> {
                    List<String> clashes = requestedSeats.stream().filter(booked::contains).toList();
                    if (!clashes.isEmpty()) {
                        return Mono.error(new BusinessValidationException(
                                "The following seat(s) are already booked: " + String.join(", ", clashes)
                                        + ". Please select different seats."));
                    }
                    BigDecimal fare = schedule.fareFor(request.getTravelClass())
                            .multiply(BigDecimal.valueOf(requestedSeats.size()));
                    Booking booking = Booking.builder()
                            .pnr(PnrGenerator.generate())
                            .userId(user.getId())
                            .scheduleId(schedule.getId())
                            .travelClass(request.getTravelClass())
                            .totalFare(fare)
                            .status(BookingStatus.CONFIRMED)
                            .bookedAt(LocalDateTime.now())
                            .build();
                    return bookingRepository.save(booking)
                            .flatMap(saved -> persistSeats(saved, schedule, requestedSeats))
                            .doOnSuccess(r -> log.info("Booking {} confirmed for {}", r.getPnr(), user.getEmail()));
                });
    }

    /** Persists the seat rows; the unique constraint is the final guard against races. */
    private Mono<BookingResponse> persistSeats(Booking saved, Schedule schedule, List<String> seats) {
        List<BookingSeat> rows = seats.stream()
                .map(seat -> BookingSeat.builder()
                        .bookingId(saved.getId())
                        .scheduleId(saved.getScheduleId())
                        .travelClass(saved.getTravelClass())
                        .seatNo(seat)
                        .build())
                .toList();
        return bookingSeatRepository.saveAll(rows)
                .then(Mono.defer(() -> toResponse(saved, schedule, seats)))
                .onErrorMap(DataIntegrityViolationException.class,
                        ex -> new BusinessValidationException(SEATS_JUST_TAKEN));
    }

    @Override
    public Flux<BookingResponse> getMyBookings(String username) {
        return currentUser(username)
                .flatMapMany(user -> bookingRepository.findByUserIdOrderByBookedAtDesc(user.getId())
                        .collectList()
                        .flatMapMany(this::toResponses));
    }

    /**
     * Batch-maps a user's bookings with a constant number of queries (seats,
     * schedules, trains) instead of several lookups per booking.
     */
    private Flux<BookingResponse> toResponses(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return Flux.empty();
        }
        Set<Long> bookingIds = bookings.stream().map(Booking::getId).collect(Collectors.toSet());
        Set<Long> scheduleIds = bookings.stream().map(Booking::getScheduleId).collect(Collectors.toSet());

        Mono<Map<Long, List<String>>> seatsMono = bookingSeatRepository.findByBookingIdIn(bookingIds)
                .collectMultimap(BookingSeat::getBookingId, BookingSeat::getSeatNo)
                .map(BookingServiceImpl::toSeatLists);

        return Mono.zip(scheduleRepository.findAllById(scheduleIds).collectMap(Schedule::getId), seatsMono)
                .flatMapMany(tuple -> {
                    Map<Long, Schedule> scheduleMap = tuple.getT1();
                    Map<Long, List<String>> seatMap = tuple.getT2();
                    Set<Long> trainIds = scheduleMap.values().stream()
                            .map(Schedule::getTrainId).collect(Collectors.toSet());
                    return trainCache.findAllById(trainIds).collectMap(Train::getId)
                            .flatMapMany(trainMap -> Flux.fromIterable(bookings).map(booking -> {
                                Schedule schedule = scheduleMap.get(booking.getScheduleId());
                                Train train = schedule != null ? trainMap.get(schedule.getTrainId()) : null;
                                List<String> seats = seatMap.getOrDefault(booking.getId(), List.of());
                                return bookingMapper.toResponse(booking, schedule, train, seats);
                            }));
                });
    }

    private static Map<Long, List<String>> toSeatLists(Map<Long, Collection<String>> grouped) {
        return grouped.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue())));
    }

    @Override
    public Mono<BookingResponse> getById(Long id, String username) {
        return currentUser(username)
                .flatMap(user -> bookingRepository.findById(id)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(BOOKING_NOT_FOUND)))
                        .filter(b -> b.getUserId().equals(user.getId()))
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(BOOKING_NOT_FOUND)))
                        .flatMap(this::toResponse));
    }

    @Override
    public Mono<byte[]> downloadTicket(Long id, String username) {
        return getById(id, username)
                .flatMap(booking -> Mono.fromCallable(() -> ticketPdfGenerator.generate(booking))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnSuccess(pdf -> log.info("Generated ticket PDF for booking id: {}", id));
    }

    @Override
    @Transactional
    public Mono<BookingResponse> cancel(Long id, String username) {
        return currentUser(username)
                .flatMap(user -> bookingRepository.findById(id)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(BOOKING_NOT_FOUND)))
                        .filter(b -> b.getUserId().equals(user.getId()))
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(BOOKING_NOT_FOUND)))
                        .flatMap(this::doCancel));
    }

    private Mono<BookingResponse> doCancel(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return Mono.error(new BusinessValidationException("This booking has already been cancelled."));
        }
        return bookingSeatRepository.findByBookingId(booking.getId())
                .map(BookingSeat::getSeatNo)
                .collectList()
                .flatMap(seats -> {
                    booking.setStatus(BookingStatus.CANCELLED);
                    return bookingSeatRepository.deleteByBookingId(booking.getId())
                            .then(bookingRepository.save(booking))
                            .flatMap(saved -> resolveResponse(saved, seats))
                            .doOnSuccess(r -> log.info("Booking {} cancelled", r.getPnr()));
                });
    }

    /** Validate seat labels, deduplicate and enforce the 1..4 rule. */
    private List<String> normalizeAndValidateSeats(BookingRequest request) {
        Set<String> unique = new LinkedHashSet<>();
        for (String raw : request.getSeatNumbers()) {
            String seat = raw == null ? null : raw.trim().toUpperCase();
            if (!SeatUtils.isValid(seat)) {
                throw new BusinessValidationException(
                        "'" + raw + "' is not a valid seat. Please select seats between 1A and 8H.");
            }
            unique.add(seat);
        }
        if (unique.size() < AppConstants.MIN_SEATS_PER_BOOKING
                || unique.size() > AppConstants.MAX_SEATS_PER_BOOKING) {
            throw new BusinessValidationException(
                    "You can book between " + AppConstants.MIN_SEATS_PER_BOOKING + " and "
                            + AppConstants.MAX_SEATS_PER_BOOKING + " seats at a time.");
        }
        return List.copyOf(unique);
    }

    /** Loads the seats for a single booking, then maps it to a response. */
    private Mono<BookingResponse> toResponse(Booking booking) {
        return bookingSeatRepository.findByBookingId(booking.getId())
                .map(BookingSeat::getSeatNo)
                .collectList()
                .flatMap(seats -> resolveResponse(booking, seats));
    }

    /** Resolves the owning schedule + train (train from cache) for the given seats. */
    private Mono<BookingResponse> resolveResponse(Booking booking, List<String> seats) {
        return scheduleRepository.findById(booking.getScheduleId())
                .flatMap(schedule -> toResponse(booking, schedule, seats))
                .switchIfEmpty(Mono.fromSupplier(() -> bookingMapper.toResponse(booking, null, null, seats)));
    }

    private Mono<BookingResponse> toResponse(Booking booking, Schedule schedule, List<String> seats) {
        return trainCache.findById(schedule.getTrainId())
                .map(train -> bookingMapper.toResponse(booking, schedule, train, seats))
                .switchIfEmpty(Mono.fromSupplier(() -> bookingMapper.toResponse(booking, schedule, null, seats)));
    }

    private Mono<User> currentUser(String username) {
        return userRepository.findByEmail(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Your account could not be found. Please sign in again.")));
    }
}
