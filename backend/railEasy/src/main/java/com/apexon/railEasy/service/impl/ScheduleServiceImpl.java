package com.apexon.railEasy.service.impl;

import com.apexon.railEasy.constants.AppConstants;
import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.request.ScheduleRequest;
import com.apexon.railEasy.dto.response.ScheduleResponse;
import com.apexon.railEasy.dto.response.SeatAvailabilityResponse;
import com.apexon.railEasy.entity.Schedule;
import com.apexon.railEasy.entity.Train;
import com.apexon.railEasy.exception.BusinessValidationException;
import com.apexon.railEasy.exception.ResourceNotFoundException;
import com.apexon.railEasy.mapper.ScheduleMapper;
import com.apexon.railEasy.repository.BookingRepository;
import com.apexon.railEasy.repository.ScheduleRepository;
import com.apexon.railEasy.repository.TrainRepository;
import com.apexon.railEasy.service.ScheduleService;
import com.apexon.railEasy.service.SeatAvailabilityHelper;
import com.apexon.railEasy.util.SeatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link ScheduleService} implementation.
 */
@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private static final String SCHEDULE_NOT_FOUND =
            "We couldn't find the requested schedule. It may have been removed.";
    private static final String TRAIN_NOT_FOUND =
            "The selected train could not be found. Please choose a valid train.";
    private static final String DUPLICATE_SLOT =
            "This train is already scheduled to depart on the selected date and time. "
                    + "Please choose a different date or departure time.";

    /** Sentinel id used for create-time duplicate checks (no schedule owns id 0). */
    private static final long NO_EXCLUSION = 0L;

    private final ScheduleRepository scheduleRepository;
    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;
    private final ScheduleMapper scheduleMapper;
    private final SeatAvailabilityHelper seatAvailabilityHelper;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepository,
                               TrainRepository trainRepository,
                               BookingRepository bookingRepository,
                               ScheduleMapper scheduleMapper,
                               SeatAvailabilityHelper seatAvailabilityHelper) {
        this.scheduleRepository = scheduleRepository;
        this.trainRepository = trainRepository;
        this.bookingRepository = bookingRepository;
        this.scheduleMapper = scheduleMapper;
        this.seatAvailabilityHelper = seatAvailabilityHelper;
    }

    @Override
    public Mono<ScheduleResponse> create(ScheduleRequest request) {
        return Mono.defer(() -> {
            validateStations(request);
            return trainRepository.findById(request.getTrainId())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException(TRAIN_NOT_FOUND)))
                    .then(Mono.defer(() -> ensureUniqueSlot(request, NO_EXCLUSION)))
                    .then(Mono.defer(() -> scheduleRepository.save(scheduleMapper.toEntity(request))))
                    .flatMap(this::enrich)
                    .doOnSuccess(s -> log.info("Created schedule for train id: {}", request.getTrainId()));
        });
    }

    @Override
    public Mono<ScheduleResponse> update(Long id, ScheduleRequest request) {
        return Mono.defer(() -> {
            validateStations(request);
            return scheduleRepository.findById(id)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException(SCHEDULE_NOT_FOUND)))
                    .flatMap(schedule -> trainRepository.findById(request.getTrainId())
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException(TRAIN_NOT_FOUND)))
                            .thenReturn(schedule))
                    .flatMap(schedule -> ensureUniqueSlot(request, schedule.getId())
                            .thenReturn(schedule))
                    .flatMap(schedule -> {
                        scheduleMapper.updateEntity(schedule, request);
                        return scheduleRepository.save(schedule);
                    })
                    .flatMap(this::enrich)
                    .doOnSuccess(s -> log.info("Updated schedule id: {}", id));
        });
    }

    @Override
    public Mono<Void> delete(Long id) {
        return scheduleRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(SCHEDULE_NOT_FOUND)))
                .flatMap(schedule -> bookingRepository.existsByScheduleId(id)
                        .flatMap(hasBookings -> Boolean.TRUE.equals(hasBookings)
                                ? Mono.<Void>error(new BusinessValidationException(
                                        "This schedule has active bookings and cannot be deleted."))
                                : scheduleRepository.delete(schedule)))
                .doOnSuccess(v -> log.info("Deleted schedule id: {}", id));
    }

    @Override
    public Mono<ScheduleResponse> getById(Long id) {
        return scheduleRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(SCHEDULE_NOT_FOUND)))
                .flatMap(this::enrich);
    }

    @Override
    public Flux<ScheduleResponse> search(String from, String to, java.time.LocalDate date) {
        return scheduleRepository.search(from, to, date).flatMap(this::enrich);
    }

    @Override
    public Flux<ScheduleResponse> getAll() {
        return scheduleRepository.findAll().flatMap(this::enrich);
    }

    @Override
    public Flux<ScheduleResponse> getByTrain(Long trainId) {
        return scheduleRepository.findByTrainId(trainId).flatMap(this::enrich);
    }

    @Override
    public Mono<SeatAvailabilityResponse> getSeatAvailability(Long scheduleId, TravelClass travelClass) {
        return scheduleRepository.findById(scheduleId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(SCHEDULE_NOT_FOUND)))
                .flatMap(schedule -> seatAvailabilityHelper.bookedSeats(scheduleId, travelClass)
                        .map(booked -> {
                            List<String> all = SeatUtils.allSeats();
                            List<String> available = all.stream().filter(s -> !booked.contains(s)).toList();
                            return SeatAvailabilityResponse.builder()
                                    .scheduleId(scheduleId)
                                    .travelClass(travelClass)
                                    .rows(AppConstants.SEAT_ROWS)
                                    .columns(AppConstants.SEAT_COLUMNS)
                                    .totalSeats(AppConstants.SEATS_PER_CLASS)
                                    .allSeats(all)
                                    .bookedSeats(booked.stream().sorted().toList())
                                    .availableSeats(available)
                                    .build();
                        }));
    }

    /** Resolves the owning train and per-class availability, then maps to a response. */
    private Mono<ScheduleResponse> enrich(Schedule schedule) {
        return trainRepository.findById(schedule.getTrainId())
                .flatMap(train -> availabilityPerClass(schedule.getId(), capacityOf(train))
                        .map(avail -> scheduleMapper.toResponse(schedule, train, avail)))
                .switchIfEmpty(availabilityPerClass(schedule.getId(), AppConstants.SEATS_PER_CLASS)
                        .map(avail -> scheduleMapper.toResponse(schedule, null, avail)));
    }

    private int capacityOf(Train train) {
        return train.getTotalSeatsPerClass() != null
                ? train.getTotalSeatsPerClass()
                : AppConstants.SEATS_PER_CLASS;
    }

    private Mono<Map<TravelClass, Integer>> availabilityPerClass(Long scheduleId, int capacity) {
        return Flux.fromArray(TravelClass.values())
                .flatMap(tc -> seatAvailabilityHelper.bookedSeats(scheduleId, tc)
                        .map(booked -> Map.entry(tc, Math.max(0, capacity - booked.size()))))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue, () -> new EnumMap<>(TravelClass.class));
    }

    private void validateStations(ScheduleRequest request) {
        if (request.getFromStation().equalsIgnoreCase(request.getToStation())) {
            throw new BusinessValidationException(
                    "The departure and destination stations must be different.");
        }
    }

    /**
     * Fails with a {@link BusinessValidationException} when the train already has a
     * schedule departing at the same date and time. The {@code excludeId} allows an
     * update to ignore the schedule currently being edited.
     */
    private Mono<Void> ensureUniqueSlot(ScheduleRequest request, long excludeId) {
        return scheduleRepository.existsDuplicateSlot(request.getTrainId(),
                        request.getJourneyDate(), request.getDepartureTime(), excludeId)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new BusinessValidationException(DUPLICATE_SLOT))
                        : Mono.empty());
    }
}



